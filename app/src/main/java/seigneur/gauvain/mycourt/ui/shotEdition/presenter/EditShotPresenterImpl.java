package seigneur.gauvain.mycourt.ui.shotEdition.presenter;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;
import retrofit2.Response;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.ui.shotEdition.view.ShotEditionViewModel;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.shotEdition.view.EditShotView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.ImagePicker;
import seigneur.gauvain.mycourt.utils.ImageUtils;
import seigneur.gauvain.mycourt.utils.MyTextUtils;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

@PerActivity
public class EditShotPresenterImpl implements EditShotPresenter {

    //Dependencies
    private EditShotView mEditShotView;
    private LifecycleOwner mLifecycleOwner;
    private  ShotEditionViewModel mShotEditionViewModel;
    private ShotDraftRepository mShotDraftRepository;
    private NetworkErrorHandler mNetworkErrorHandler;
    private ConnectivityReceiver mConnectivityReceiver;
    private ShotRepository mShotRepository;
    private TempDataRepository mTempDataRepository; //may be this one could be removed in future

    //Data, these info have to be managed by ViewModel
    //Manage data
    //private int mSource;
    private String mTags;
    private ArrayList<String> mTagList;
    private String mShotTitle;
    private String mShotDescription;
    private ShotDraft mShotDraft;
    private Shot mShot;
    private int mEditionMode;
    //RX disposable
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * Constructor
     * Presenter is not injected as I don"t know how inject ViewModel in it correctly
     * @param editShotView
     * @param lifecycleOwner
     * @param shotEditionViewModel
     * @param shotDraftRepository
     * @param networkErrorHandler
     * @param connectivityReceiver
     * @param shotRepository
     * @param tempDataRepository
     */
    public EditShotPresenterImpl(EditShotView editShotView,
                                 LifecycleOwner lifecycleOwner,
                                 ShotEditionViewModel shotEditionViewModel,
                                 ShotDraftRepository shotDraftRepository,
                                 NetworkErrorHandler networkErrorHandler,
                                 ConnectivityReceiver connectivityReceiver,
                                 ShotRepository shotRepository,
                                 TempDataRepository tempDataRepository) {
        this.mEditShotView=editShotView;
        this.mLifecycleOwner=lifecycleOwner;
        this.mShotEditionViewModel=shotEditionViewModel;
        this.mShotDraftRepository=shotDraftRepository;
        this.mNetworkErrorHandler=networkErrorHandler;
        this.mConnectivityReceiver=connectivityReceiver;
        this.mShotRepository=shotRepository;
        this.mTempDataRepository=tempDataRepository;
    }

    /**************************************************************************
     * EditShotPresenter Implementation
     *************************************************************************/
    @Override
    public void onAttach() {
        getSourceType();

        //Observes ViewModel datas to display last value on UI
        mShotEditionViewModel.getImageCroppedUri() //
                .observe(
                        mLifecycleOwner,
                        uri -> {
                            Timber.tag("viewModelTest").d("uri changed: "+uri);
                            if (mEditShotView!=null)
                                mEditShotView.displayShotImagePreview(uri);
                        }
                );
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
        if (requestCode==Constants.PICK_IMAGE_REQUEST) {
            if (mEditShotView!=null) {
                Uri imagePickedUriSource = ImagePicker.getImageUriFromResult(context, resultCode, data);
                String imagePickedFileName = ImagePicker.getPickedImageName(context, imagePickedUriSource);
                Timber.tag("007bond").d(imagePickedFileName);
                mShotEditionViewModel.imagePickedFormat.setValue(ImageUtils.getImageExtension(context, imagePickedUriSource));
                //imagePickedFormat= ImageUtils.getImageExtension(context, imagePickedUriSource);
                int[] imageSize= ImageUtils.imagePickedWidthHeight(context, imagePickedUriSource, 0);
                mEditShotView.goToUCropActivity(
                        mShotEditionViewModel.getImagePickedFormat().getValue(),
                        imagePickedUriSource,
                         imagePickedFileName,
                        imageSize);
            }
        }
    }


    @Override
    public void onImageCropped(int requestCode, int resultCode, Intent data) {
        if (requestCode== UCrop.REQUEST_CROP) {
            mShotEditionViewModel.imageCroppedUri.setValue(UCrop.getOutput(data));
            mShotEditionViewModel.isImageChanged.setValue(true);
            //isImageChanged=true;
        } else if (resultCode == UCrop.RESULT_ERROR) {
            //isImageChanged=false;
            mShotEditionViewModel.isImageChanged.setValue(false);
            final Throwable cropError = UCrop.getError(data);
            Timber.d(cropError);
        }
    }

    @Override
    public void onPermissionGranted(Context context) {
        //if (imageCroppedUri!=null) {
        if (mShotEditionViewModel.getImageCroppedUri().getValue()!=null) {
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
        createTagList(tempTagList());
        if (mEditShotView!=null && isFromFab) //else is from the dialog called from onAbort method.
            mEditShotView.openConfirmMenu();
    }

    @Override
    public void onDraftShotClicked(Context context) {
        if (getTitle()==null || getTitle()!=null && getTitle().isEmpty())
            mEditShotView.showMessageEmptyTitle();
        else {
            //if (imageCroppedUri!=null && imagePickedFormat!=null) {
            if (mShotEditionViewModel.getImageCroppedUri().getValue()!=null &&
                    mShotEditionViewModel.getImagePickedFormat().getValue()!=null) {
                if (mEditShotView!=null)
                    mEditShotView.checkPermissionExtStorage();
            } else {
                registerOrUpdateDraft(context,false);
            }
        }
    }

    @Override
    public void onPublishClicked(Context context) {
        if (isAuthorizedToPublish()) {
            if (mEditionMode==Constants.EDIT_MODE_NEW_SHOT) {
                postShot(context, getImageUri(),getImageFormat(),getTitle(), getShotDescription(),
                        getTagList());
            } else if (mEditionMode==Constants.EDIT_MODE_UPDATE_SHOT) {
                updateShot(getTitle(), getShotDescription(), getTagList(), getProfile());
            }
        } else {
            //todo - make a view call
            Timber.d("not allowed to publish or update");
        }

    }

    @Override
    public void onAbort(boolean isMenuOpen) {
        //todo : check if user has started to write or download a pic
        //if yes : warn him
        //if no : do nothing but close activity
       if (mEditShotView !=null) {
           if (isMenuOpen)
               mEditShotView.openConfirmMenu(); //in fact close it
           //else if (isStartEditing())
       }

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
            mShotTitle= title;
        else
            mShotTitle="";
    }

    @Override
    public void onDescriptionChanged(String description) {
        if (description.length()>0)
            mShotDescription= description;
    }

    @Override
    public void onTagChanged(String tags) {
       if (!tags.isEmpty()) {
           mTags = tags;
       }
    }

    /*
     *************************************************************************
     * get data source type on opening activity
     *************************************************************************/
    // Check whether if the activity is opened from draft registered in database or other
    private void getSourceType() {
        compositeDisposable
                .add(Single.just(mTempDataRepository.getDraftCallingSource())
                .subscribe(
                        this::getDataFromSourceType,
                        this::manageSourceTypeError
                )
        );
    }

    private void getDataFromSourceType(int source) {
        mShotEditionViewModel.mSource.setValue(source);
        //mSource= source;
        Timber.d("calling source: " +source);
        if (source == Constants.SOURCE_DRAFT) {
            //user click on a saved shotdraft
            getShotDraft();
        } else if (source == Constants.SOURCE_SHOT) {
            //User wishes to update a published shot
            setEditionMode(Constants.EDIT_MODE_UPDATE_SHOT);
            getShot();
        } else if (source == Constants.SOURCE_FAB) {
            //User wishes to create a shot
            setEditionMode(Constants.EDIT_MODE_NEW_SHOT);
            if (mEditShotView!=null) {
                mEditShotView.setUpShotCreationUI();
            }
        } else {
            Timber.d("impossible error happened! wait wut?");
        }
    }

    private void manageSourceTypeError(Throwable throwable) {
        Timber.d(throwable);
        //mSource=-1;
        mShotEditionViewModel.mSource.setValue(-1); //todo - check this!
    }

    /*
     *************************************************************************
     * get shot if data source equals == SOURCE_SHOT
     *************************************************************************/
    private void getShot() {
        compositeDisposable.add(Single.just(mTempDataRepository.getShot())
                .subscribe(
                        this::manageShotInfo,
                        this::doOngetShotError
                )
        );

    }

    private void manageShotInfo(Shot shot) {
        mShot=shot;
        if (mEditShotView!=null)
            mEditShotView.setUpShotEditionUI(shot, getEditMode());
    }

    private void doOngetShotError(Throwable throwable) {
        Timber.d(throwable);
    }

    /*
     *************************************************************************
     * get ShotDraft if data source equals == SOURCE_DRAFT
     *************************************************************************/
    private void getShotDraft() {
        compositeDisposable.add(Single.just(mTempDataRepository.getShotDraft())
                .subscribe(
                        this::manageShotDraftInfo,
                        this::doOnGetShotDraftError
                )
        );
    }

    private void manageShotDraftInfo(ShotDraft shotDraft) {
        setShotDraft(shotDraft);
        setEditionMode(shotDraft.getDraftType());
        if (mEditShotView!=null)
            mEditShotView.setUpShotEditionUI(shotDraft,getEditMode());
    }

    private void doOnGetShotDraftError(Throwable throwable) {
        Timber.d(throwable);
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
        MultipartBody.Part body = prepareFilePart(context,fileUri,imageFormat,"image");
        //add to HashMap key and RequestBody
        HashMap<String, RequestBody> map = new HashMap<>();
        // executes the request
        compositeDisposable.add(
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
    private void updateShot(String title, String desc, ArrayList<String> tags, boolean profile) {
        compositeDisposable.add(
                mShotRepository.updateShot(
                        getShotId(),
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
        if (mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_DRAFT)
            deleteDraft();
        else
        if (mEditShotView!=null)
            mEditShotView.stopActivity();
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

    /*
     *************************************************************************
     * DB OPERATIONS - REGISTER OR UPDATE SHOTDRAFT
     *************************************************************************/
    private void registerOrUpdateDraft(Context context,boolean isRegisteringImage) {
        if (mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_DRAFT) {
            if (isRegisteringImage)
                storeDraftImage(
                        mShotEditionViewModel.getImagePickedFormat().getValue(),
                        mShotEditionViewModel.getImageCroppedUri().getValue(),
                        context);
                //storeDraftImage(imagePickedFormat, imageCroppedUri, context);
            else
                updateInfoDraft(mShotDraft.getImageUrl(), mShotDraft.getImageFormat());
        } else if (mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_SHOT) {
            saveInfoDraft(mShot.getImageUrl(), null);
        } else if (mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_FAB) {
            if (isRegisteringImage)
                //storeDraftImage(imagePickedFormat,imageCroppedUri,context);
                storeDraftImage(mShotEditionViewModel.getImagePickedFormat().getValue(),
                        mShotEditionViewModel.getImageCroppedUri().getValue(),
                        context);
            else
                saveInfoDraft( null, null);
        }
    }

    /**
     * Store image in "My Court" folder in external storage and get the URI to save it in DB
     * @param imageCroppedFormat - jpeg, png, gif
     * @param croppedFileUri - uri of the image
     * @param context - EditShot Activity
     */
    private void storeDraftImage(String imageCroppedFormat, Uri croppedFileUri, Context context) {
        Timber.tag("imageCroppedUri").d(mShotEditionViewModel.getImageCroppedUri().getValue().toString());
        compositeDisposable.add(mShotDraftRepository.storeImageAndReturnItsUri(imageCroppedFormat,croppedFileUri,context)
                .onErrorResumeNext(t -> t instanceof NullPointerException ? Single.error(t):Single.error(t)) //todo : to comment this
                .subscribe(
                        imageSaved -> saveDraftInDB(imageSaved, imageCroppedFormat),
                        this::doOnCopyImageError
                )
        );
    }

    /**
     * Save draft in db
     * @param imageStorageURL - string to be saved in db
     * @param imageCroppedFormat - jpeg, png, gif
     */
    private void saveDraftInDB(String imageStorageURL,String imageCroppedFormat)  {
        if (mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_DRAFT) {
            updateInfoDraft(imageStorageURL,imageCroppedFormat);
        } else {
            saveInfoDraft(imageStorageURL,imageCroppedFormat);
        }
    }

    /**
     * Indicates to user that an error occurred while trying to copy Image in MyCourt folder
     * @param t - throwable
     */
    private void doOnCopyImageError(Throwable t)  {
        Timber.d(t);
    }

    /**
     * save draft information in DB (all info except image)
     * @param imageUri - image url
     * @param imageFormat - jpeg, png, gif
     */
    private void saveInfoDraft(@Nullable String imageUri,@Nullable String imageFormat) {
        compositeDisposable.add(
                mShotDraftRepository.storeShotDraft(
                        createShotDraft(0,imageUri,imageFormat))
                        .subscribe(
                            this::onDraftSaved,
                            this::onDraftSavingError
                        )
        );
    }

    /**
     * update draft information in DB (all info except image)
     * @param imageUri - image url
     * @param imageFormat - jpeg, png, gif
     */
    private void updateInfoDraft(@Nullable String imageUri, @Nullable String imageFormat) {
        compositeDisposable.add(mShotDraftRepository.updateShotDraft(
                createShotDraft(mShotDraft.getId(),imageUri,imageFormat))
                .subscribe(
                        this::onDraftSaved,
                        this::onDraftSavingError
                )
        );
    }

    /**
     * Draft has been saved/updated in DB
     */
    private void onDraftSaved() {
        mTempDataRepository.setDraftsChanged(true);
        if (mEditShotView!=null)
            mEditShotView.notifyPostSaved();
    }

    /**
     * An error occurred while trying to saved draft in db
     * @param t - throwable
     */
    private void onDraftSavingError(Throwable t) {
        Timber.d(t);
    }

    /**
     * Delete draft after has been published or updated on Dribbble
     */
    private void deleteDraft() {
        compositeDisposable.add(
                mShotDraftRepository.deleteDraft(mShotDraft.getId())
                        .subscribe(
                                this::onDraftDeleted,
                                this::onDeleteDraftFailed
                        )
        );
    }

    /**
     * Draft has been deleted correctly
     */
    private void onDraftDeleted() {
        mTempDataRepository.setDraftsChanged(true);
        if (mEditShotView!=null)
            mEditShotView.stopActivity();
    }

    /**
     * An error happened during delete process
     * @param t - error description
     */
    private void onDeleteDraftFailed(Throwable t) {
        Timber.d(t);
    }

    /**
     * Create Shot draft object to insert or update a ShotDraft
     * @param primaryKey - unique identifier
     * @param imageUri - image url
     * @param imageFormat _ jpeg, png, gif
     * @return ShotDraft
     */
    private ShotDraft createShotDraft(int primaryKey, @Nullable String imageUri, @Nullable String imageFormat) {
        return new  ShotDraft(
                primaryKey,
                imageUri,
                imageFormat,
                getShotId(),
                getTitle(),
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

    /*
     *************************************************************************
     * Get data from UI and from data source
     * to create shot or draft and perform network and DB operation
     *************************************************************************/
    public String getTitle() {
        return mShotTitle;
    }

    public ArrayList<String> getTagList() {
        return mTagList;
    }

    //Create taglist according to Dribbble pattern
    private ArrayList<String> tempTagList() {
        ArrayList<String> tempList = new ArrayList<>();
        //create the list just one time, not any time the tags changed
        if (mTags!=null && !mTags.isEmpty()) {
            Pattern p = Pattern.compile(MyTextUtils.tagRegex);
            Matcher m = p.matcher(mTags);
            if (MyTextUtils.isDoubleQuoteCountEven(mTags)) {
                // number is even or 0
                while (m.find()) {
                    tempList.add(m.group(0));
                }
            }
            else {
                //todo-  number is odd: warn user and stop
            }
        }
        return tempList;
    }

    private static ArrayList<String> tagListWithoutQuote(ArrayList<String> list){
        String[] output = new String[list.size()];
        StringBuilder builder;
        for(int i=0;i<list.size();i++){
            builder = new StringBuilder();
            output[i] = builder.toString();
            output[i]= list.get(i).replaceAll("\"", "");
        }

        return new ArrayList<>(Arrays.asList(output));
    }

    private void createTagList(ArrayList<String> tempList){
        mTagList = tagListWithoutQuote(tempList);
    }

    public String getShotDescription() {
        return mShotDescription;
    }

    private int getEditMode() {
        return mEditionMode;
    }

    private Uri getImageUri() {
        //todo - can be change with ViewModel
        if (mEditionMode==Constants.EDIT_MODE_UPDATE_SHOT) {
            if(mShotDraft!=null && mShotDraft.getImageUrl()!=null) {
                if (mShotEditionViewModel.getImageChanged()!=null &&
                        mShotEditionViewModel.getImageChanged().getValue()!=null &&
                        !mShotEditionViewModel.getImageChanged().getValue())
                    return Uri.parse(mShotDraft.getImageUrl());
                else
                    //return imageCroppedUri;
                    return  mShotEditionViewModel.getImageCroppedUri().getValue();
            } else {
                //if it is not a ShotDraft it is a shot
                return Uri.parse(mShot.getImageUrl());
            }
        } else {
            //return imageCroppedUri;
            return mShotEditionViewModel.getImageCroppedUri().getValue();
        }
    }

    private String getImageFormat() {
        if(mShotEditionViewModel.getImageChanged()!=null &&
                mShotEditionViewModel.getImageChanged().getValue()!=null &&
                !mShotEditionViewModel.getImageChanged().getValue() &&
                mShotDraft!=null && mShotDraft.getImageFormat()!=null)
            return mShotDraft.getImageFormat();
        else
            return mShotEditionViewModel.getImagePickedFormat().getValue();
    }

    //Todo - following methods are not good, manage this in another way
    private boolean getProfile(){
        if(mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_SHOT)
            return mShot.isLow_profile();
        if(mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_DRAFT)
            return mShotDraft.isLowProfile();
        else
            return false;
    }

    //Todo - manage this from UI (only of new draft)
    private Date getDateOfPublication(){
        if(mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_SHOT)
            return mShot.getPublishDate();
        if(mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_DRAFT)
            return mShotDraft.getDateOfPublication();
        else
            return null;
    }

    //info that can't be change
    private String getShotId(){
        if(mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_SHOT)
            return mShot.getId();
        if(mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_DRAFT)
            return mShotDraft.getShotId();
        else
            return "undefined";
    }

    private Date getDateOfupdate(){
        if(mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_SHOT)
            return mShot.getUpdateDate();
        if(mShotEditionViewModel.getSource().getValue()!=null &&
                mShotEditionViewModel.getSource().getValue()==Constants.SOURCE_DRAFT)
            return mShotDraft.getDateOfUpdate();
        else
            return null;
    }

    //Info related to Edition
    private boolean isStartEditing() {
        return false;
    }

    private void setEditionMode(int editionMode) {
        mEditionMode = editionMode;
    }

    private void setShotDraft(ShotDraft shotDraft) {
        mShotDraft = shotDraft;
    }

    private boolean isAuthorizedToPublish() {
        return getTitle()!=null && !getTitle().isEmpty() && getImageUri()!=null;
    }

}
