package seigneur.gauvain.mycourt.ui.shotEdition.tasks

import android.content.Context
import android.net.Uri
import io.reactivex.Observable
import io.reactivex.Single.just

import java.io.IOException
import java.net.UnknownHostException
import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import okhttp3.Headers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
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

    //todo - if attachments, on success do it :
    //1 - load the shot ! check that the name is the same as the one published, or get the id from the response!
    //2 - with the id perform attachment operation
    fun postShot(draft: Draft,
                 context: Context,
                 fileUri: Uri,
                 imageFormat: String) {
        val body = HttpUtils.createFilePart(context, fileUri, imageFormat, "image")
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
                                Timber.tag("jul").d("UnknownHostException, dafuck")
                            }
                            handleNetworkOperationError(t, 100)
                        }
                        .subscribe(
                                { response -> onPostSucceed(response, draft) },
                                {t -> onPostFailed(t)}
                        )
        )
    }

    /**
     * Post operation has succeed - check the response from server
     * response must be 202 to be publish on Dribbble
     * @param response - body response from Dribbble
     */
    private fun onPostSucceed(response: Response<Void>, draft: Draft) {
        when (response.code()) {
            Constants.ACCEPTED -> {
                val headers = response.headers()
                //todo - use location get shot id and send attachment after with RX
                val location = headers.get("location")
                if (location != null)
                    Timber.d("post succeed. location: $location")
                onPublishOrUpdateSucceed(draft)
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
    * NETWORK OPERATION - ADD ATTACHMENT TO A SHOT
    *************************************************************************/
    /**
    * Dribbbles API doesn't allow to publish a shot with attachments.
    * We can only provide attachments to an existing shot
    * Also, we can send only one attachment per POST.
    * So for first publication with attachment, we must concat it in two request
    */

    /**
     * Post one or several attachments to an existing shot
     */
    fun postAttachments(
            id:String,
            context: Context,
            uris:List<Uri>,
            imageFormat: String) {
        mCompositeDisposable.add(
                Observable.just(uris) //we create an Observable that emits a single array
                        .flatMapIterable { it} //map the list to an Observable that emits every item as an observable
                        .flatMap {it -> //perform following operation on every item
                            // create RequestBody instance from file
                            val body = HttpUtils.createFilePart(context, it, imageFormat, "file")
                            mShotRepository.addAttachment(
                                    id,
                                    body)
                                    .doOnNext{
                                        response -> Timber.d(response.message())
                                    }
                        }
                        .subscribe(
                                { response -> Timber.d(response.message())},
                                {t -> onPostFailed(t)},
                                {Timber.d("complete")}
                        )
        )
    }

    /**
     * Post an attachment to an existing shot
     */
    fun postAttachment(
            id :String,
            context: Context,
            fileUri: Uri,
            imageFormat: String) {
        // create RequestBody instance from file
        val body = HttpUtils.createFilePart(context, fileUri, imageFormat, "file")
        // executes the request
        mCompositeDisposable.add(
                mShotRepository.addAttachment(
                        id,
                        body)
                        .doOnError { t ->
                            Timber.tag("postAttachment").d("error: "+t)
                        }
                        .subscribe(
                                { response -> Timber.d(response.message())},
                                {t -> onPostFailed(t)}
                        )
        )
    }

    /*
    *************************************************************************
    * NETWORK OPERATION - UPDATE SHOT ON DRIBBBLE
    *************************************************************************/
    fun updateShot(
            draft: Draft,
            profile: Boolean) {
        mCompositeDisposable.add(
                mShotRepository.updateShot(
                        draft.shot.id!!, //get it from viewmodel
                        draft.shot.title!!,
                        draft.shot.description!!,
                        draft.shot.tagList!!,
                        profile)
                        .subscribe(
                                { shot ->  onUpdateShotSuccess(shot, draft) },
                                { this.onUpdateShotError(it)}
                        )
        )
    }

    /**
     * Shot update succeed
     * @param shot - shot updated
     */
    private fun onUpdateShotSuccess(shot: Shot,draft: Draft) {
        //todo must finish with a code to send to Main Activity to delete the draft
        Timber.d("success: " + shot.title!!)
        onPublishOrUpdateSucceed(draft)
    }

    /**
     * Manage network error while trying to perform update
     * @param throwable - throwable
     */
    private fun onUpdateShotError(throwable: Throwable) {
        //todo - manage UI
    }

    /**
     * manage UI and DB items on Post/Updated Succeed
     */
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
