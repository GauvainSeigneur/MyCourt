package seigneur.gauvain.mycourt.ui.shotEdition.tasks

import android.content.Context
import android.net.Uri
import java.io.IOException
import java.util.HashMap
import io.reactivex.disposables.CompositeDisposable
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
    * POST SHOT ON DRIBBBLE
    *************************************************************************/
    fun postShot(draft: Draft,
                 context: Context) {
        if (draft!=null) {
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
        } else {
            //draft object lost....
        }


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
            Constants.ACCEPTED -> { doOnPostAccepted(response, draft, context) }
            else -> doOnPostRejected(response)
        }
    }

    private fun doOnPostAccepted(response: Response<Void>,
                                  draft: Draft,
                                  context: Context) {
        val headers = response.headers()
        val location = headers.get("location")
        location?.let { Timber.d("post succeed. location: $location") }
        if (draft.hasAttachmentToPublish()) {
            val shotId:String //todo - this variable must be defined into Firebase for fast update
            val locationTrunkAfter= location!!.substringAfterLast("/",location )
            shotId=locationTrunkAfter.substringBefore("-",locationTrunkAfter)
            postAttachments( shotId, context, draft)
        } else {
            //stop process and confirm to user that the post has been successfully published
            onPublishSucceed(draft)
        }
    }

    //todo - test it goes directly in error if code is not 202
    private fun doOnPostRejected(response: Response<Void>) {
        onPublishFailed("Post failed: " + response.code() +": "+ response.message())
    }

    /**
     * An error occurred while trying to make post request
     * @param t - throwable
     */
    private fun onPostFailed(t: Throwable) {
        onPublishFailed("Post failed: $t")
    }

    /*
    *************************************************************************
    * UPDATE SHOT ON DRIBBBLE
    *************************************************************************/
    fun updateShot(
            draft: Draft,
            attachmentTodelete:List<Attachment>,
            context: Context,
            profile: Boolean) {
        mCompositeDisposable.add(
                mShotRepository.updateShot(
                        draft.shot.id!!,
                        draft.shot.title!!,
                        draft.shot.description!!,
                        draft.shot.tagList!!,
                        profile)
                        .subscribe(
                                { shot ->
                                    onUpdateShotSuccess(shot, draft, context, attachmentTodelete)
                                },
                                { this.onUpdateShotError(it)}
                        )
        )
    }

    /**
     * Shot update succeed
     * check if we have some attachment to add or delete and eprform related operation
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
            onPublishSucceed(draft)
        }

    }

    /**
     * Manage network error while trying to perform update
     * @param throwable - throwable
     */
    private fun onUpdateShotError(throwable: Throwable) {
        onPublishFailed("Update failed: $throwable")
    }
    /*
    *************************************************************************
    * POST ATTACHMENT
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
                                {onPublishSucceed(draft)}
                        )
        )

    }

    private fun onPosAttachmentSucceed(response: Response<Void>, draft: Draft) {
        Timber.d("post attachment succeed:" +response.message())
        onPublishSucceed(draft)
    }

    /*
    *************************************************************************
    * DELETE AN ATTACHMENT
    *************************************************************************/
    private fun deleteAttachments(draft: Draft, listAttachmentToDelete:List<Attachment>) {
        mCompositeDisposable.add(mShotRepository.deleteAttachment(draft,listAttachmentToDelete)
                .subscribe(
                        { onDeleteAttachmentSucceed(it, draft)},
                        {t -> onDeleteAttachmentFailed(t, draft)}
                )
        )
    }

    private fun onDeleteAttachmentSucceed(response: Response<Void>, draft: Draft) {
        Timber.d("delete attachment succeed:" +response.message())
        onPublishSucceed(draft)
    }

    private fun onDeleteAttachmentFailed(throwable: Throwable, draft: Draft) {
        onDeleteAttachmentFailed("delete attachment failed: $throwable")

        //todo - terminate UI process
       //onPublishSucceed(draft)
    }

    /*
    *************************************************************************
    * Concat post and delete attachment operation
    *************************************************************************/
    private fun postAndDeleteAttachment(draft: Draft,
                                       context: Context,
                                       shotId:String,
                                        listAttachmentToDelete: List<Attachment>) {
        mCompositeDisposable.add(
                mShotRepository.postAndDeleteAttachment(context,draft, shotId, listAttachmentToDelete)
                        .subscribe(
                                { response -> onPublishSucceed(draft) },
                                {t -> onPostFailed(t)},
                                { onPublishSucceed(draft)}
                        )
        )
    }

    /*
    *************************************************************************
    * DB OPERATION - DELETE DRAFT AFTER PUBLISH OR UPDATE
    *************************************************************************/
    private fun deleteDraftAfterPublish(draft:Draft) {
        mCompositeDisposable.add(mShotDraftRepository.deleteDraft(draft.draftID)
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
    * GLOBAL METHOD FOR EVERY REQUEST AND POST
    *************************************************************************/
    private fun onPublishSucceed(draft:Draft) {
        deleteDraftAfterPublish(draft) //todo
        mPublishCallBack.onPublishSucceed()
    }

    private fun onPublishFailed(error:String) {
        mPublishCallBack.onPublishFailed(error)
    }

    private fun onDeleteAttachmentFailed(error:String) {
        mPublishCallBack.onDeleteAttachmentFailed(error)
    }

    /*
    *************************************************************************
    * MANAGE NETWORK EXCEPTION
    * TODO - test it
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

        fun onPublishSucceed()

        fun onPublishFailed(error:String)

        fun onDeleteAttachmentFailed(error:String)

    }

}
