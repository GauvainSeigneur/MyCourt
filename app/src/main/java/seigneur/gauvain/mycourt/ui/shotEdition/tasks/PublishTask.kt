package seigneur.gauvain.mycourt.ui.shotEdition.tasks

import android.content.Context
import android.net.Uri
import io.reactivex.Observable
import io.reactivex.Single.just
import io.reactivex.android.schedulers.AndroidSchedulers

import java.io.IOException
import java.net.UnknownHostException
import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.Headers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
import seigneur.gauvain.mycourt.data.model.Attachment
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository
import seigneur.gauvain.mycourt.data.repository.ShotRepository
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.HttpUtils
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler
import timber.log.Timber


class PublishTask(
        private val mCompositeDisposable: CompositeDisposable,
        private val mShotRepository: ShotRepository,
        private val mShotDraftRepository: ShotDraftRepository,
        private val mNetworkErrorHandler: NetworkErrorHandler?,
        private val mConnectivityReceiver: ConnectivityReceiver,
        private val mPublishCallBack: PublishCallBack) {

    /*
    *************************************************************************
    * NETWORK OPERATION - POST SHOT ON DRIBBBLE
    *************************************************************************/
    //1 - load the shot ! check that the name is the same as the one published, or get the id from the response!
    //2 - with the id perform attachment operation
    fun postShot(draft: Draft,
                 context: Context) {
        val body = HttpUtils.createShotFilPart(
                context,
                draft.croppedImgDimen!!,
                Uri.parse(draft.imageUri),
                draft.imageFormat, "image")
        //add to HashMap key and RequestBody
        val map = HashMap<String, RequestBody>()
        // executes the request
        mCompositeDisposable.add(
                mShotRepository.publishANewShot(
                        map,
                        body,
                        draft.shot.title,
                        draft.shot.description,
                        draft.shot.tagList)
                        .doOnError { t ->
                            if (t is IOException) {
                                Timber.d("UnknownHostException: $t")
                            }
                            handleNetworkOperationError(t, 100)
                        }
                        .subscribe(
                                { response -> onPostSucceed(response, draft, context) },
                                {t -> onPostFailed(t)}
                        )
        )
    }

    /**
     * Post operation has succeed - check the response from server
     * response must be 202 to be publish on Dribbble
     * @param response - body response from Dribbble
     */
    private fun onPostSucceed(response: Response<Void>,
                              draft: Draft,
                              context: Context) {
        when (response.code()) {
            Constants.ACCEPTED -> {
                val headers = response.headers()
                val location = headers.get("location")
                location?.let { Timber.d("post succeed. location: $location") }
                if (draft.hasAttachmentToPublish()) {
                    val shotId:String //todo - this variable must be defined into Firebase for fast update
                    val locationTrunkAfter= location!!.substringAfterLast("/",location )
                    shotId=locationTrunkAfter.substringBefore("-",locationTrunkAfter)
                    Timber.d("shotid: $shotId")
                    postAttachments( shotId, context, draft)
                } else {
                    //stop process and confirm to user that the post has been successfully published
                    onPublishOrUpdateSucceed(draft)
                }
            }
            else -> Timber.d("post not succeed: " + response.code())
        }
    }

    /**
     * An error occurred while trying to make post request
     * @param t - throwable
     */
    private fun onPostFailed(t: Throwable) {
        Timber.d("post failed: $t")
    }

    /*
    *************************************************************************
    * NETWORK OPERATION - UPDATE SHOT ON DRIBBBLE
    *************************************************************************/
    fun updateShot(
            draft: Draft,
            attachmentTodelete:List<Attachment>,
            context: Context,
            profile: Boolean) {
        mCompositeDisposable.add(
                mShotRepository.updateShot(
                        draft.shot.id!!, //get it from viewmodel
                        draft.shot.title!!,
                        draft.shot.description!!,
                        draft.shot.tagList!!,
                        profile)
                        .subscribe(
                                { shot ->
                                    //postOrDeleteAttachment(draft, context, draft.shot.id!!)
                                    onUpdateShotSuccess(shot, draft, context, attachmentTodelete)
                                },
                                { this.onUpdateShotError(it)}
                        )
        )
    }

    /**
     * Shot update succeed
     * @param shot - shot updated
     */
    private fun onUpdateShotSuccess(shot: Shot,
                                    draft: Draft,
                                    context: Context,
                                    attachmentTodelete: List<Attachment>) {
        Timber.d("success: " + shot.title!!)
        if (!draft.hasAttachmentToPublish() && !attachmentTodelete.isNullOrEmpty()) {
                Timber.d("update ope: delete attachment")
               deleteAttachments(draft, attachmentTodelete)
        } else if (draft.hasAttachmentToPublish() && attachmentTodelete.isNullOrEmpty()) {
            Timber.d("update ope: post attachment")
            postAttachments(shot.id!!, context, draft)
        } else if (draft.hasAttachmentToPublish() && !attachmentTodelete.isNullOrEmpty()) {
            postAndDeleteAttachment(draft, context, shot.id!!, attachmentTodelete)
        } else {
            onPublishOrUpdateSucceed(draft)
        }

    }

    /**
     * Manage network error while trying to perform update
     * @param throwable - throwable
     */
    private fun onUpdateShotError(throwable: Throwable) {
        //todo - manage UI
    }
    /*
    *************************************************************************
    * NETWORK OPERATION - get a shot after a publishing
    *
    * Dribbbles API doesn't allow to publish a shot with attachments.
    * We can only provide attachments to an existing shot
    * Also, we can send only one attachment per POST.
    * So for first publication with attachment, we must concat it in two request
    *************************************************************************/
    private fun postAttachments(
            shotId:String,
            context: Context,
            draft: Draft) {
        mCompositeDisposable.add(
                mShotRepository.postAttachment(context, draft, shotId)
                        .subscribe(
                                { onPosAttachmentSucceed(it, draft)},
                                {t -> onPostFailed(t)},
                                {onPublishOrUpdateSucceed(draft)}
                        )
        )

    }

    private fun onPosAttachmentSucceed(response: Response<Void>, draft: Draft) {
        Timber.d("post attachment succeed:" +response.message())
        onPublishOrUpdateSucceed(draft)
    }

    /*
    *************************************************************************
    * DELETE AN ATTACHMENT
    *************************************************************************/
    private fun deleteAttachments(draft: Draft, listAttachmentToDelete:List<Attachment>) {
        mCompositeDisposable.add(mShotRepository.deleteAttachment(draft,listAttachmentToDelete)
                .subscribe(
                        { onDeleteAttachmentSucceed(it, draft)},
                        {t -> onPostFailed(t)},
                        {onPublishOrUpdateSucceed(draft)}
                )
        )
    }

    private fun onDeleteAttachmentSucceed(response: Response<Void>, draft: Draft) {
        Timber.d("delete attchment succeed:" +response.message())
        onPublishOrUpdateSucceed(draft)
    }

    private fun onDeleteAttachmentFailed(throwable: Throwable, draft: Draft) {

    }

    /*
    *************************************************************************
    * Concat post and delete attachment operation
    * todo -finalize and manage error on first and second observale
    * todo - here t replace post and delete attachment methods by only one
    *************************************************************************/
    private fun postAndDeleteAttachment(draft: Draft,
                                       context: Context,
                                       shotId:String,
                                        listAttachmentToDelete: List<Attachment>) {
        mCompositeDisposable.add(
                mShotRepository.postAndDeleteAttachment(context,draft, shotId, listAttachmentToDelete)
                        .subscribe(
                                { response -> onPublishOrUpdateSucceed(draft) },
                                {t -> onPostFailed(t)},
                                { onPublishOrUpdateSucceed(draft)}
                        )
        )
    }

    /*
    *************************************************************************
    * GLOBAL METHOD FOR EVERY REQUEST AND POST
    *************************************************************************/
    private fun onPublishOrUpdateSucceed(draft:Draft) {
        deleteDraftAfterPublish(draft)
        mPublishCallBack.onPublishSuccess()
    }

    /*
    *************************************************************************
    * DB OPERATION - DELETE DRAFT AFTER PUBLISH OR UPDATE
    *************************************************************************/
    private fun deleteDraftAfterPublish(draft:Draft) {
        mCompositeDisposable.add( mShotDraftRepository.deleteDraft(draft.draftID)
                .subscribe(
                        this::onDeleteSucceed,
                        this::onDeleteError
                )
        )

    }

    private fun onDeleteSucceed() {
        Timber.d("delete succeed")
    }

    private fun onDeleteError(throwable: Throwable) {
        Timber.e(throwable)
    }

    /*
     *************************************************************************
     * MANAGE NETWORK EXCEPTION
     *************************************************************************/
    private fun handleNetworkOperationError(error: Throwable, eventID: Int) {
        Timber.tag("rxHandler").d("error handled by rx ma gueule")
        if (mNetworkErrorHandler == null) {
            Timber.tag("rxHandler").d("mNetworkErrorHandler is null")
        } else {
            Timber.tag("rxHandler").d("error is : " + error.javaClass)
            mNetworkErrorHandler.handleNetworkErrors(error, eventID, object : NetworkErrorHandler.onRXErrorListener {
                override fun onUnexpectedException(throwable: Throwable) {
                    Timber.tag("rxHandler").d("unexpected error happened, don't know what to do...")
                }

                override fun onNetworkException(throwable: Throwable) {
                    if (mConnectivityReceiver.isOnline()) {
                        Timber.tag("rxHandler").d("it seems that you have unexpected errors")
                    } else {
                        Timber.tag("rxHandler").d("Not connected to internet, so it is normal that you have an error")
                    }

                }

                override fun onHttpException(throwable: Throwable) {
                    Timber.tag("rxHandler").d(throwable)
                    if ((throwable as HttpException).code() == 403) {
                        //todo - access forbidden
                    }
                }
            })
        }

    }

    /**
     * CALLBACK FOR VIEWMODEL
     */
    interface PublishCallBack {
        fun onPublishSuccess()
    }

}
