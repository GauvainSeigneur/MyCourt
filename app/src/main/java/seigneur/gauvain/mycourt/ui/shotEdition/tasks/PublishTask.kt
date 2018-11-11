package seigneur.gauvain.mycourt.ui.shotEdition.tasks

import android.content.Context
import android.net.Uri

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
    fun postShot(context: Context,
                 fileUri: Uri,
                 imageFormat: String,
                 titleString: String,
                 descriptionString: String,
                 tagList: ArrayList<String>) {
        val body = HttpUtils.createFilePart(context, fileUri, imageFormat, "image")
        //add to HashMap key and RequestBody
        val map = HashMap<String, RequestBody>()
        // executes the request
        mCompositeDisposable.add(
                mShotRepository.publishANewShot(
                        map,
                        body,
                        titleString,
                        descriptionString,
                        tagList)
                        .doOnError { t ->
                            if (t is IOException) {
                                Timber.tag("jul").d("UnknownHostException, dafuck")
                            }
                            handleNetworkOperationError(t, 100)
                        }
                        .subscribe(
                                this::onPostSucceed,
                                this::onPostFailed
                        )
        )
    }

    /**
     * Post operation has succeed - check the response from server
     * response must be 202 to be publish on Dribbble
     * @param response - body response from Dribbble
     */
    private fun onPostSucceed(response: Response<Void>) {
        when (response.code()) {
            Constants.ACCEPTED -> {
                val headers = response.headers()
                //todo - use location get shot id and send attachment after with RX
                val location = headers.get("location")
                if (location != null)
                    Timber.d("post succeed. location: $location")
                onPublishOrUpdateSucceed()
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
            shotId: String,
            title: String, desc: String, tags: ArrayList<String>, profile: Boolean) {
        mCompositeDisposable.add(
                mShotRepository.updateShot(
                        shotId, //get it from viewmodel
                        title,
                        desc,
                        tags,
                        profile)
                        .subscribe(
                                this::onUpdateShotSuccess,
                                this::onUpdateShotError
                        )
        )
    }

    /**
     * Shot update succeed
     * @param shot - shot updated
     */
    private fun onUpdateShotSuccess(shot: Shot) {
        //todo must finish with a code to send to Main Activity to delete the draft
        Timber.d("success: " + shot.title!!)
        onPublishOrUpdateSucceed()
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
    private fun onPublishOrUpdateSucceed() {
        mPublishCallBack.onPublishSuccess()
        /*
        if (source==Constants.SOURCE_DRAFT) {
            //deleteDraft();  // reactivate
        }
        else {
            //todo listener
            //if (mEditShotView!=null)
            //    mEditShotView.stopActivity();
        }*/
    }


    /*
     *************************************************************************
     * DB OPERATION - DELETE DRAFT AFTER PUBLISH OR UPDATE
     *************************************************************************/
    private fun deleteDraft() {
        /*mCompositeDisposable.add(
                mShotDraftRepository.deleteDraft(((ShotDraft) mObjectSource).getId())
                        .subscribe(
                                this::onDraftDeleted,
                                this::onDeleteDraftFailed
                        )
        );*/
    }

    /**
     * Draft has been deleted correctly
     */
    private fun onDraftDeleted() {
        //mTempDataRepository.setDraftsChanged(true);//TODO LIVE DATA
        //TODO SINGLE LIVE EVENT
        /*if (mEditShotView!=null)
            mEditShotView.stopActivity();*/
    }

    /**
     * An error happened during delete process
     *
     * @param t - error description
     */
    private fun onDeleteDraftFailed(t: Throwable) {
        Timber.d(t)
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
