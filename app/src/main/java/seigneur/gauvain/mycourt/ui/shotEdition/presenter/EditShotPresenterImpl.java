package seigneur.gauvain.mycourt.ui.shotEdition.presenter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.HttpException;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.shotEdition.view.EditShotActivity;
import seigneur.gauvain.mycourt.ui.shotEdition.view.EditShotView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.ImagePicker;
import seigneur.gauvain.mycourt.utils.ImageUtils;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

@PerActivity
public class EditShotPresenterImpl implements EditShotPresenter {

    @Inject
    EditShotView mEditShotView;

    @Inject
    ShotDraftRepository mShotDraftRepository;

    @Inject
    NetworkErrorHandler mNetworkErrorHandler;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    ShotRepository mShotRepository;

    @Inject
    TempDataRepository mTempDataRepository;

    private ShotDraft mShotDraft;
    private Shot mShot;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int mEditionMode;

    //Manage Image picking and cropping
    private Uri imagePickedUriSource = null;
    private String imagePickedFileName = null;
    private String imagePickedformat = null;
    private Uri imageCroppedUri = null;
    private Uri imageSavedUri = null;
    private int mSource;
    private String mTags;
    private ArrayList<String> tagList;
    private String shotTitle;
    private String shotDescription;


    @Inject
    public EditShotPresenterImpl() {
    }

    @Override
    public void onAttach() {
        getSource();
    }

    @Override
    public void onDetach() {
        compositeDisposable.dispose();
        mEditShotView =null;
    }

    @Override
    public void onTagLimitReached() {
        Timber.d("onTagLimitReached called");
    }

    @Override
    public void onImagePicked(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode==Constants.PICK_IMAGE_ID) {
            Timber.d(imagePickedformat);
            if (mEditShotView!=null) {
                imagePickedFileName = ImagePicker.getImageNameFromResult(context, resultCode);
                imagePickedUriSource = ImagePicker.getImageUriFromResult(context, resultCode, data);
                imagePickedformat= ImageUtils.getImageExtension(imagePickedUriSource, context);
                mEditShotView.goToUCropActivity(imagePickedformat, imagePickedUriSource,imagePickedFileName);
            }
        }
    }

    @Override
    public void onImageCropped(int requestCode, int resultCode, Intent data) {
        if (requestCode== UCrop.REQUEST_CROP) {
            imageCroppedUri = UCrop.getOutput(data); //get Image Uri after being cropped
            if (mEditShotView!=null) {
                mEditShotView.displayShotImagePreview(imageCroppedUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Timber.d(cropError);
        }
    }

    @Override
    public void onPermissionGranted(Context context) {
        if (imageCroppedUri!=null) {
            if (mEditShotView!=null) {
                registerOrUpdateDraft(context, true);
            }
        }
    }

    @Override
    public void onPermissionDenied() {
        if (mEditShotView!=null)
            mEditShotView.requestPermission();
    }

    @Override
    public void onConfirmEditionClicked(boolean isFromFab) {
        //create the list just one time, not any time the tags changed
        if (mTags!=null && mTags.length()>0)
            tagList = new ArrayList<String>(Arrays.asList(mTags.split(",")));
            Timber.d(tagList+"");
        if (mEditShotView!=null && isFromFab) //else is from the dialog called from onAbort method.
                mEditShotView.openConfirmMenu();

    }

    @Override
    public void onDraftShotClicked(Context context) {
        if (shotTitle==null || shotTitle.isEmpty())
            mEditShotView.showMessageEmptyTitle();
        else {
            if (imageCroppedUri!=null && imagePickedformat!=null) {
                if (mEditShotView!=null)
                    mEditShotView.checkPermissionExtStorage();
            } else {
                registerOrUpdateDraft(context,false);
            }
        }
    }

    @Override
    public void onPublishClicked(Shot shot) {
        if (mEditionMode==Constants.EDIT_MODE_NEW_SHOT) {
            publishShot(shot);
        } else if (mEditionMode==Constants.EDIT_MODE_UPDATE_SHOT) {
            updateShot(shot);
        }
    }

    @Override
    public void onAbort() {
        //todo : check if user has started to write or download a pic
        //if yes : warn him
        //if no : do nothing but close activity
    }

    @Override
    public void onIllustrationClicked() {
        if (mEditShotView!=null) {
            if (mEditionMode==Constants.EDIT_MODE_NEW_SHOT)
                mEditShotView.openImagePicker();
            else
                mEditShotView.showImageNotUpdatable();
        }
    }

    @Override
    public void onTitleChanged(String title) {
        if (title.length()>0)
            shotTitle= title;
        else
            shotTitle="";
    }

    @Override
    public void onDescriptionChanged(String description) {
        if (description.length()>0)
            shotDescription= description;
    }

    @Override
    public void onTagChanged(String tags) {
       if (tags.length()>0)
           mTags = tags;
    }

    private void publishShot(Shot shot) {

    }

    private void registerOrUpdateDraft(Context context,boolean isRegisteringImage) {
        if (mSource==Constants.SOURCE_DRAFT) {
            if (isRegisteringImage)
                storeDraftImage(imagePickedformat,imageCroppedUri,context);
            else
                updateInfoDraft(mShotDraft.getImageUrl());
        } else if (mSource==Constants.SOURCE_SHOT) {
            saveInfoDraft(mShot.getImageUrl());
        } else if (mSource==Constants.SOURCE_FAB) {
            if (isRegisteringImage)
                storeDraftImage(imagePickedformat,imageCroppedUri,context);
            else
                saveInfoDraft(null);
        }

    }


    /**************************************************************************
     * get data source on opening activity
     *************************************************************************/
    // Check whether if the activity is opened from draft registered in database or other
    private void getSource() {
        compositeDisposable.add(Single.just(mTempDataRepository.getDraftCallingSource())
                .doOnSuccess(source-> {
                    mSource= source;
                    Timber.d("calling source: " +source);
                    if (source == Constants.SOURCE_DRAFT) {
                        //user click on a saved shotdraft
                        getShotDraft();
                    } else if (source == Constants.SOURCE_SHOT) {
                        //User wishes to update a published shot
                        mEditionMode=Constants.EDIT_MODE_UPDATE_SHOT;
                        getShot();
                    } else if (source == Constants.SOURCE_FAB) {
                        //User wishes to create a shot
                        mEditionMode=Constants.EDIT_MODE_NEW_SHOT;
                        if (mEditShotView!=null) {
                            mEditShotView.setUpShotCreationUI();
                        }
                    } else {
                        //can't happen
                    }
                })
                .doOnError(t -> {
                    mSource=-1;
                })
                .subscribe()
        );
    }

    /**
     * get the shot selected
     */
    private void getShot() {
        compositeDisposable.add(Single.just(mTempDataRepository.getShot())
                .doOnSuccess(shot-> {
                    Timber.d(shot.title);
                    mShot=shot;
                    if (mEditShotView!=null) {
                        mEditShotView.setUpShotEdtionUI(shot,null);
                    }
                })
                .doOnError(t -> {

                })
                .subscribe()
        );

    }

    /**
     * get the ShotDraft selected
     */
    private void getShotDraft() {
        compositeDisposable.add(Single.just(mTempDataRepository.getShotDraft())
                .doOnSuccess(shotDraft-> {
                    Timber.d("shotdraft id: "+shotDraft.id+"");
                    mEditionMode=shotDraft.getDraftType();
                    mShotDraft = shotDraft;
                    if (mEditShotView!=null) {
                        mEditShotView.setUpShotEdtionUI(null,shotDraft);
                    }
                })
                .doOnError(t -> {

                })
                .subscribe()
        );
    }

    /*************************************************************************
     * STORING DRAFTS IN DB
     *************************************************************************/
    private void storeDraftImage(String imageCroppedFormat, Uri croppedFileUri, Context context) {
        compositeDisposable.add(mShotDraftRepository.storeImageAndReturnItsUri(imageCroppedFormat,croppedFileUri,context)
                //.onErrorResumeNext(whenExceptionIsThenIgnore(IllegalArgumentException.class))
                .onErrorResumeNext(t -> t instanceof NullPointerException ? Single.error(t):Single.error(t))
                .doOnSuccess(uriImageSaved-> {
                    if (mSource==Constants.SOURCE_DRAFT) {
                        updateInfoDraft(uriImageSaved.toString());
                    } else {
                        saveInfoDraft(uriImageSaved.toString());
                    }
                })
                .doOnError(t -> {
                    //todo - reactivate for release!
                    /*RxJavaPlugins.setErrorHandler(e ->{
                        Timber.w("Undeliverable exception received, not sure what to do", e);
                        Toast.makeText(context, "Undeliverable exception: "+e, Toast.LENGTH_SHORT).show();
                        }
                    );*/
                    //to deactivate and move Ui function in RxJavaPlugins.setErrorHandler in production
                    Toast.makeText(context, "failed: "+t, Toast.LENGTH_SHORT).show();
                })
                .subscribe()
        );
    }

    private void updateInfoDraft(@Nullable String imageUri) {
        compositeDisposable.add(mShotDraftRepository.updateShotDraft(createShotDraft(mShotDraft.getId(),imageUri))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(id -> {
                    if (mEditShotView!=null)
                        mEditShotView.notifyPostSaved();
                })
                .doOnError(t->{

                })
                .subscribe()
        );
    }

    private void saveInfoDraft(@Nullable String imageUri) {
        compositeDisposable.add(
                mShotDraftRepository.storeShotDraft(createShotDraft(0,imageUri)
                )
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(id -> {
                            if (mEditShotView!=null)
                                mEditShotView.notifyPostSaved();
                        })
                        .doOnError(t->{

                        })
                        .subscribe()
        );
    }

    /*************************************************************************
     * Create shotDraft object to save or update in database
     *************************************************************************/
    private ShotDraft createShotDraft(int primarykey, @Nullable String imageUri) {
        return new  ShotDraft(
                primarykey,
                imageUri,
                getShotId(),
                shotTitle,
                getShotDescription(),
                getProfile(),
                null, //todo - for phase 2 : allow user to schedule publishing
                getTagList(),
                -1, //todo - for phase 2 : manage team
                getEditMode(),
                getDateOfPublication(),
                getDateOfupdate()
        );
    }

    public int getEditMode() {
        return mEditionMode;
    }

    private boolean getProfile(){
        if(mSource==Constants.SOURCE_SHOT)
            return mShot.isLow_profile();
        if(mSource==Constants.SOURCE_DRAFT)
            return mShotDraft.isLowProfile();
        else
            return false;
    }

    private String getShotId(){
        if(mSource==Constants.SOURCE_SHOT)
            return mShot.getId();
        if(mSource==Constants.SOURCE_DRAFT)
            return mShotDraft.getShotId();
        else
            return "undefined";
    }

    private Date getDateOfPublication(){
        if(mSource==Constants.SOURCE_SHOT)
            return mShot.getPublishDate();
        if(mSource==Constants.SOURCE_DRAFT)
            return mShotDraft.getDateOfPublication();
        else
            return null;
    }

    private Date getDateOfupdate(){
        if(mSource==Constants.SOURCE_SHOT)
            return mShot.getUpdateDate();
        if(mSource==Constants.SOURCE_DRAFT)
            return mShotDraft.getDateOfUpdate();
        else
            return null;
    }

    public ArrayList<String> getTagList() {
        return tagList;
    }

    public String getShotDescription() {
        return shotDescription;
    }
    /*************************************************************************
     * PUBLISH OPERATION
     * MANAGE UPDATE AND POST IN ONE OBSERVABLE
     *************************************************************************/
    private void publish(Shot shot) {
        //todo - one observable which make two ope :
        //1 - create a shot object with the right info
        //2 - POST or PUT according to edition mode
        Shot shotFrorPublish= new Shot();
        //set info acording to what we have like this : shot.setLow_profile(false);
        //and then : try to publish
        if (mEditionMode==Constants.EDIT_MODE_NEW_SHOT) {
            publishShot(shot);
        } else if (mEditionMode==Constants.EDIT_MODE_UPDATE_SHOT) {
            updateShot(shot);
        }
    }
    /*************************************************************************
     * Create shot object for update and publishing
     *************************************************************************/


    /*************************************************************************
     * UPDATE
     *************************************************************************/
    private void updateShot(Shot shot) {
        compositeDisposable.add(
                mShotRepository.updateShot(
                        shot.getId(),
                        shot.description,
                        shot.isLow_profile(),
                        shot.title)
                        .doOnSuccess(new Consumer<Shot>() {
                            @Override
                            public void accept(Shot shot) throws Exception {
                                Timber.d("success: "+shot.getTitle());
                            }
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable error) throws Exception {
                                handleRetrofitError(error);
                            }
                        })
                        .subscribe()
        );

    }
    /*************************************************************************
     * POST SHOT
     *************************************************************************/
    private void postShot(Shot shot) {
        File file = new File(imageCroppedUri.getPath());
        // creates RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/"+imagePickedformat), file);
    }

    /*************************************************************************
     * MANAGE NETWORK OPERATION EXCEPTION
     *************************************************************************/
    private void handleRetrofitError(final Throwable error) {
        mNetworkErrorHandler.handleNetworkErrors(error,new NetworkErrorHandler.onRXErrorListener() {
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


}
