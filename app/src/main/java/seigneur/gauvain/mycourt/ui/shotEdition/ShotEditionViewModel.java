package seigneur.gauvain.mycourt.ui.shotEdition;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.ui.shotEdition.tasks.GetSourceTask;
import seigneur.gauvain.mycourt.ui.shotEdition.tasks.StoreDraftTask;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.MyTextUtils;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

/**
 * PRESENTER REFACTORING INTO VIEWMODEL
 * FILES TO BIG - Difficult to read
 * split it into task files : fetch source and set up UI / store / update / publish
 */
public class ShotEditionViewModel extends ViewModel implements
        StoreDraftTask.StoreRequestListener,
        GetSourceTask.SourceCallback {

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

    @Inject
    Application mApplication;

    //RX disposable
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    //Manage source and Edition mode (new or update)
    private int mSource = -1;
    private int mEditionMode = -1;
    private Object mObjectSource = null;
    private SingleLiveEvent<Void> mSetUpUiCmd = new SingleLiveEvent<>();

    //Pick and Crop Image
    private SingleLiveEvent<Void> mPickShotCommand = new SingleLiveEvent<>();
    private SingleLiveEvent<Void> mCropImageCmd = new SingleLiveEvent<>();
    public Uri imagePickedUriSource = null; //NOT LIVEDATA - NOT RELATED TO UI
    public String imagePickedFileName = null; //NOT LIVEDATA - NOT RELATED TO UI
    public String imagePickedFormat = null; //NOT LIVEDATA - NOT RELATED TO UI
    public int[] imageSize = null; //NOT LIVEDATA - NOT RELATED TO UI
    private MutableLiveData<Uri> croppedImageUri = new MutableLiveData<>();
    private SingleLiveEvent<Integer> pickCropImgErrorCmd = new SingleLiveEvent<>();
    //Listen change in editText
    private MutableLiveData<String> mTitle = new MutableLiveData<>();       //todo - MAY BE NOT LIVEDATA as Edit Text UI survive to config change
    private MutableLiveData<String> mDescription = new MutableLiveData<>();  //todo - MAY BE NOT LIVEDATA as Edit Text UI survive to config change
    private MutableLiveData<ArrayList<String>> mTags = new MutableLiveData<>();   //todo - MAY BE NOT LIVEDATA as Edit Text UI survive to config change

    //task files
    private StoreDraftTask mStoreDrafTask;
    private GetSourceTask mGetSourceTask;

    @Inject
    public ShotEditionViewModel() {
        //Task have same scope of ViewModel
        mStoreDrafTask = new StoreDraftTask(this);
        mGetSourceTask = new GetSourceTask(this);
    }

    @Override
    public void onCleared() {
        super.onCleared();
        Timber.d("viewmodel cleared");
        compositeDisposable.clear();
    }

    /*
     *********************************************************************************************
     * EVENT WHICH VIEW WILL SUBSCRIBE
     *********************************************************************************************/
    public SingleLiveEvent<Void> getPickShotCommand() {
        return mPickShotCommand;
    }

    public SingleLiveEvent<Void> getCropImageCmd() {
        return mCropImageCmd;
    }

    public SingleLiveEvent<Integer> getPickCropImgErrorCmd() {
        return pickCropImgErrorCmd;
    }

    public SingleLiveEvent<Void> getSetUpUiCmd() {
        return mSetUpUiCmd;
    }

    public LiveData<Uri> getCroppedImageUri() {
        return croppedImageUri;
    }

    public LiveData<String> getTitle() {
        return mTitle;
    }

    public LiveData<String> getDescription() {
        return mDescription;
    }

    public LiveData<ArrayList<String>> getTags() {
        return mTags;
    }

    /*
    *********************************************************************************************
    * PUBLIC METHODS CALLED IN VIEW
    *********************************************************************************************/
    public void init() {
        if (croppedImageUri.getValue() == null)
            croppedImageUri.setValue(null); //just notify UI
        //here set as LiveData all data from source
        if (mSource == -1)
            mGetSourceTask.getOriginOfEditRequest(compositeDisposable, mTempDataRepository);
    }

    public void onImagePreviewClicked() {
        mPickShotCommand.call();
    }

    public void onImagePicked() {
        mCropImageCmd.call();
    }

    public void onImageCropped(Uri uri) {
        croppedImageUri.setValue(uri);
    }

    public void onPickcropError(int erroCode) {
        pickCropImgErrorCmd.setValue(erroCode);
    }

    public void onTitleChanged(String title) {
        mTitle.setValue(title);
    }

    public void onDescriptionChanged(String desc) {
        mDescription.setValue(desc);
    }

    public void onTagChanged(String tag) {
        mTags.setValue(tagListWithoutQuote(tag));
    }

    public void onStoreDraftClicked() {
        registerOrUpdateDraft(mApplication, false);
        /*if (getTitle().getValue() == null || getTitle().getValue().isEmpty()) {
            //TODO - single live event
            //mEditShotView.showMessageEmptyTitle();
        } else {
            if (croppedImageUri.getValue() != null && imagePickedFormat != null) {
                //TODO - single live event
                //mEditShotView.checkPermissionExtStorage();
            } else {
                registerOrUpdateDraft(mApplication, false);
            }
        }*/
    }

    /*
    *********************************************************************************************
    * GETTER AND SETTER - CAN BE CALLED BY VIEW AND VIEWMODEL
    *********************************************************************************************/
    public Uri getImagePickedUriSource() {
        return imagePickedUriSource;
    }

    public void setImagePickedUriSource(Uri imagePickedUriSource) {
        this.imagePickedUriSource = imagePickedUriSource;
    }

    public String getImagePickedFileName() {
        return imagePickedFileName;
    }

    public void setImagePickedFileName(String imagePickedFileName) {
        this.imagePickedFileName = imagePickedFileName;
    }

    public String getImagePickedFormat() {
        return imagePickedFormat;
    }

    public void setImagePickedFormat(String imagePickedFormat) {
        this.imagePickedFormat = imagePickedFormat;
    }

    public int[] getImageSize() {
        return imageSize;
    }

    public void setImageSize(int[] imageSize) {
        this.imageSize = imageSize;
    }

    public int getEditionMode() {
        return mEditionMode;
    }

    public void setEditionMode(int mEditionMode) {
        this.mEditionMode = mEditionMode;
    }

    public Object getObjectSource() {
        return mObjectSource;
    }

    public void setObjectSource(Object mObjectSource) {
        this.mObjectSource = mObjectSource;
    }

    public int getSource() {
        return mSource;
    }

    public void setSource(int mSource) {
        this.mSource = mSource;
    }

    /*
     *********************************************************************************************
     * STORE DRAFT IN DB
     *********************************************************************************************/
    private void registerOrUpdateDraft(Context context, boolean isRegisteringImage) {
        //FOR TESTS ONLY
        mStoreDrafTask.saveDraft(
                compositeDisposable,
                mShotDraftRepository,
                null,
                null,
                getShotId(getSource()),
                getTitle().getValue(),
                getDescription().getValue(),
                getProfile(getSource()),
                getTags().getValue(),
                getEditionMode(),
                getDateOfPublication(getSource()),
                getDateOfUpdate(getSource()));
        //TODO - REACTIVATE
        /*if (mSource == Constants.SOURCE_DRAFT) {
            if (isRegisteringImage)
                storeDraftImage(getImagePickedFormat(), getCroppedImageUri().getValue(), context);
            else
                updateInfoDraft(((ShotDraft) mObjectSource).getImageUrl(), ((ShotDraft) mObjectSource).getImageFormat());
        } else if (mSource == Constants.SOURCE_SHOT) {
            saveInfoDraft(((Shot) mObjectSource).getImageUrl(), null);
        } else if (mSource == Constants.SOURCE_FAB) {
            if (isRegisteringImage)
                storeDraftImage(imagePickedFormat, getCroppedImageUri().getValue(), context);
            else
                saveInfoDraft(null, null);
        }*/
    }

    /**
     * Delete draft after has been published or updated on Dribbble
     */
    private void deleteDraft() {
        compositeDisposable.add(
                mShotDraftRepository.deleteDraft(((ShotDraft) mObjectSource).getId())
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
    *********************************************************************************************
    * CREATE DRAFT OBJECT - todo may be move it in task and just get a ShotDraft object from this file
    *********************************************************************************************/
    //info that can't be change
    private String getShotId(int source) {
        switch (source) {
            case Constants.SOURCE_SHOT:
                return ((Shot) mObjectSource).getId();
            case Constants.SOURCE_DRAFT:
                return ((ShotDraft) mObjectSource).getShotId();
            default:
                return "undefined";
        }
    }

    //todo - for future, manage it in UI
    private boolean getProfile(int source) {
        switch (source) {
            case Constants.SOURCE_SHOT:
                return ((Shot) mObjectSource).isLow_profile();
            case Constants.SOURCE_DRAFT:
                return ((ShotDraft) mObjectSource).isLowProfile();
            default:
                return false;
        }
    }

    //Todo - manage this from UI (only of new draft)
    private Date getDateOfPublication(int source) {
        switch (source) {
            case Constants.SOURCE_SHOT:
                return ((Shot) mObjectSource).getPublishDate();
            case Constants.SOURCE_DRAFT:
                return ((ShotDraft) mObjectSource).getDateOfPublication();
            default:
                return null;
        }
    }


    private Date getDateOfUpdate(int source) {
        switch (source) {
            case Constants.SOURCE_SHOT:
                return ((Shot) mObjectSource).getUpdateDate();
            case Constants.SOURCE_DRAFT:
                return ((ShotDraft) mObjectSource).getDateOfUpdate();
            default:
                return null;
        }
    }

    /*
    *********************************************************************************************
    * GetSourceTaskCallback
    *********************************************************************************************/
    @Override
    public void source(int source) {
        setSource(source);
    }

    @Override
    public void EditMode(int mode) {
        setEditionMode(mode);
    }

    @Override
    public void dataForUIReady() {
        mSetUpUiCmd.call();
    }

    @Override
    public void objectSource(Object object) {
        setObjectSource(object);
    }

    /*
    *********************************************************************************************
    * StoreTaskCallback
    *********************************************************************************************/
    @Override
    public void onSaveImageSuccess() {

    }

    @Override
    public void onStoreDraftSucees() {

    }

    @Override
    public void onUpdateDraftSuccess() {

    }

    @Override
    public void onFailed() {

    }

    /*
     *********************************************************************************************
     * UTILS   METHODS - TODO - move it into utils file!!!
     *********************************************************************************************/
    private static ArrayList<String> tagListWithoutQuote(String tagString) {
        ArrayList<String> listWithQuote = tempTagList(tagString);
        String[] output = new String[listWithQuote.size()];
        StringBuilder builder;
        for (int i = 0; i < listWithQuote.size(); i++) {
            builder = new StringBuilder();
            output[i] = builder.toString();
            output[i] = listWithQuote.get(i).replaceAll("\"", "");
        }

        return new ArrayList<>(Arrays.asList(output));
    }

    //Create taglist according to Dribbble pattern
    private static ArrayList<String> tempTagList(String tagString) {
        ArrayList<String> tempList = new ArrayList<>();
        //create the list just one time, not any time the tags changed
        if (tagString != null && !tagString.isEmpty()) {
            Pattern p = Pattern.compile(MyTextUtils.tagRegex);
            Matcher m = p.matcher(tagString);
            if (MyTextUtils.isDoubleQuoteCountEven(tagString)) {
                // number is even or 0
                while (m.find()) {
                    tempList.add(m.group(0));
                }
            } else {
                //todo-  number is odd: warn user and stop
            }
        }
        return tempList;
    }


}
