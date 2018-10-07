package seigneur.gauvain.mycourt.ui.shotEdition.tasks;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;
import retrofit2.Response;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.HttpUtils;
import seigneur.gauvain.mycourt.utils.image.ImageUtils;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

public class PublishTask {

    private CompositeDisposable mCompositeDisposable;

    private ShotRepository mShotRepository;

    private NetworkErrorHandler mNetworkErrorHandler;

    private ConnectivityReceiver mConnectivityReceiver;

    private PublishTaskCallBack mPublishTaskCallBack;

    private ShotDraftRepository mShotDraftRepository;

    @Inject
    public PublishTask(CompositeDisposable compositeDisposable,
                       ShotRepository shotRepository,
                       ShotDraftRepository shotDraftRepository,
                       NetworkErrorHandler networkErrorHandler,
                       ConnectivityReceiver connectivityReceiver,
                       PublishTaskCallBack publishTaskCallBack) {
        this.mCompositeDisposable=compositeDisposable;
        this.mShotRepository=shotRepository;
        this.mShotDraftRepository=shotDraftRepository;
        this.mNetworkErrorHandler=networkErrorHandler;
        this.mConnectivityReceiver=connectivityReceiver;
        this.mPublishTaskCallBack=publishTaskCallBack;
    }

    /*
    *************************************************************************
    * NETWORK OPERATION - POST SHOT ON DRIBBBLE
    *************************************************************************/
    private void postShot(Context context,
                          Uri fileUri,
                          String imageFormat,
                          String titleString,
                          String descriptionString,
                          ArrayList<String> tagList) {
        MultipartBody.Part body = HttpUtils.createFilePart(context,fileUri,imageFormat,"image");
        //add to HashMap key and RequestBody
        HashMap<String, RequestBody> map = new HashMap<>();
        // executes the request
        mCompositeDisposable.add(
                mShotRepository.publishANewShot(
                        map,
                        body,
                        titleString,
                        descriptionString,
                        tagList)
                        .subscribe(
                                this::onPostSucceed,
                                this::onPostFailed
                        )
        );
    }



    /**
     * Post operation has succeed - check the response from server
     * response must be 202 to be publish on Dribbble
     * @param response - body response from Dribbble
     */
    private void onPostSucceed(Response<Shot> response) {
        switch(response.code()) {
            case Constants.ACCEPTED:
                Timber.d("post succeed");
                //todo - do something in UI
                onPublishOrUpdateSucceed();
                break;
            default:
                Timber.d("post not succeed: "+response.code());
        }
    }

    /**
     * An error occurred while trying to make post request
     * @param t - throwable
     */
    private void onPostFailed(Throwable t) {
        Timber.e(t);
        handleNetworkOperationError(t,-1);
    }

    /*
    *************************************************************************
    * NETWORK OPERATION - UPDATE SHOT ON DRIBBBLE
    *************************************************************************/
    private void updateShot(
            String shotId,
            String title, String desc, ArrayList<String> tags, boolean profile) {
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
        );
    }

    /**
     * Shot update succeed
     * @param shot - shot updated
     */
    private void onUpdateShotSuccess(Shot shot) {
        //todo must finish with a code to send to Main Activity to delete the draft
        Timber.d("success: "+shot.getTitle());
        onPublishOrUpdateSucceed();
    }

    /**
     * Manage network error while trying to perform update
     * @param throwable - throwable
     */
    private void onUpdateShotError(Throwable throwable) {
        //todo - manage UI
        handleNetworkOperationError(throwable,-1);
    }

    /**
     * manage UI and DB items on Post/Updated Succeed
     */
    private void onPublishOrUpdateSucceed() {
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
    private void deleteDraft() {
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
    private void onDraftDeleted() {
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
    private void onDeleteDraftFailed(Throwable t) {
        Timber.d(t);
    }

    /*
    *************************************************************************
    * MANAGE NETWORK EXCEPTION
    *************************************************************************/
    private void handleNetworkOperationError(final Throwable error, int eventID) {
        mNetworkErrorHandler.handleNetworkErrors(error,eventID, new NetworkErrorHandler.onRXErrorListener() {
            @Override
            public void onUnexpectedException(Throwable throwable) {
                Timber.d("unexpected error happened, don't know what to do...");
            }

            @Override
            public void onNetworkException(Throwable throwable) {
                Timber.d(throwable);
                if (mConnectivityReceiver.isOnline()) {
                    Timber.d("it seems that you have unexpected errors");
                } else {
                    Timber.d("Not connected to internet, so it is normal that you have an error");
                }

            }

            @Override
            public void onHttpException(Throwable throwable) {
                Timber.tag("HttpNetworks").d(throwable);
                if (((HttpException) throwable).code() == 403) {
                    //todo - access forbidden
                }
            }

        });
    }

    /**
     * CALLBACK FOR VIEWMODEL
     */
    public interface PublishTaskCallBack {

        void onTestGood();
    }


}
