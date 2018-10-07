package seigneur.gauvain.mycourt.ui.shotEdition.tasks;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;
import retrofit2.Response;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.ImageUtils;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

public class PublishTask {

    private NetworkErrorHandler mNetworkErrorHandler;

    private ConnectivityReceiver mConnectivityReceiver;

    private PublishTaskCallBack mPublishTaskCallBack;

    @Inject
    public PublishTask(NetworkErrorHandler networkErrorHandler,
                       ConnectivityReceiver connectivityReceiver,
                       PublishTaskCallBack publishTaskCallBack) {
        this.mNetworkErrorHandler=networkErrorHandler;
        this.mConnectivityReceiver=connectivityReceiver;
        this.mPublishTaskCallBack=publishTaskCallBack;
    }

    public void test() {
       if (mConnectivityReceiver!=null) {
           mPublishTaskCallBack.onTestGood();
           Timber.d("mConnectivityReceiver not null");
       }
        else
            Timber.d("null");
    }

    /*
    *************************************************************************
    * NETWORK OPERATION - POST SHOT ON DRIBBBLE
    *************************************************************************/
    private void postShot(
            CompositeDisposable compositeDisposable,
            ShotRepository shotRepository,
            Context context,
                          Uri fileUri,
                          String imageFormat,
                          String titleString,
                          String descriptionString,
                          ArrayList<String> tagList) {
        MultipartBody.Part body = prepareFilePart(context,fileUri,imageFormat,"image");
        //add to HashMap key and RequestBody
        HashMap<String, RequestBody> map = new HashMap<>();
        // executes the request
        compositeDisposable.add(
                shotRepository.publishANewShot(
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
     * Create MultipartBody.Part instance separated in order to use @PartMap annotation
     * to pass parameters along with File request. PartMap is a Map of "Key" and RequestBody.
     * See : https://stackoverflow.com/a/40873297
     */
    private MultipartBody.Part prepareFilePart(Context context, Uri fileUri,String imageFormat, String partName) {
        //Get file
        String uriOfFile = ImageUtils.getRealPathFromImage(context,fileUri);
        File file = new File(uriOfFile);
        String imageFormatFixed;
        //Word around for jpg format - refused by dribbble
        if (imageFormat.equals("jpg")) imageFormatFixed="JPG"; // to be tested
        else imageFormatFixed =imageFormat;
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/"+imageFormatFixed), file);
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
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
            CompositeDisposable compositeDisposable, ShotRepository shotRepository,
            String shotId,
            String title, String desc, ArrayList<String> tags, boolean profile) {
        compositeDisposable.add(
                shotRepository.updateShot(
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
