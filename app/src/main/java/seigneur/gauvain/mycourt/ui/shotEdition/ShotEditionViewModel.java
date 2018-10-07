package seigneur.gauvain.mycourt.ui.shotEdition;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.ui.shotEdition.tasks.GetSourceTask;
import seigneur.gauvain.mycourt.ui.shotEdition.tasks.PublishTask;
import seigneur.gauvain.mycourt.ui.shotEdition.tasks.StoreDraftTask;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.ListUtils;
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
        GetSourceTask.SourceCallback,
        PublishTask.PublishTaskCallBack {

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
    private SingleLiveEvent<Void> mRequestPermCmd = new SingleLiveEvent<>();
    private SingleLiveEvent<Void> mCheckPerm = new SingleLiveEvent<>();
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
    private PublishTask mPublishTask;

    @Inject
    public ShotEditionViewModel() {
    }

    @Override
    public void onCleared() {
        super.onCleared();
        Timber.d("viewmodel cleared");
        compositeDisposable.clear();
    }


    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN VIEW
     *********************************************************************************************/
    public void init() {
        initTasks();
        if (croppedImageUri.getValue() == null)
            croppedImageUri.setValue(null); //just notify UI
        //here set as LiveData all data from source
        if (mSource == -1)
            mGetSourceTask.getOriginOfEditRequest();
    }

    private void initTasks() {
        if (mStoreDrafTask!=null && mPublishTask!=null && mGetSourceTask!=null) {
            Timber.d("tasks initialized");
        } else {

            Timber.d("tasks null");
        }

        if (mPublishTask==null)
            mPublishTask=new PublishTask(compositeDisposable, mShotRepository, mShotDraftRepository,
                    mNetworkErrorHandler, mConnectivityReceiver,
                    this);
        if (mStoreDrafTask==null)
            mStoreDrafTask  = new StoreDraftTask(compositeDisposable, mShotDraftRepository,this);
        if (mGetSourceTask==null)
            mGetSourceTask  = new GetSourceTask(mTempDataRepository, compositeDisposable,this);

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
        mTags.setValue(ListUtils.tagListWithoutQuote(tag));
    }

    public void requestPerm() {
        mRequestPermCmd.call();
    }

    public void onPermGranted() {
        registerOrUpdateDraft(mApplication, true);
    }

    public void onStoreDraftClicked() {
        if (getCroppedImageUri().getValue()!=null && getImagePickedFormat() !=null) {
            mCheckPerm.call();
        } else {
            registerOrUpdateDraft(mApplication, false);
        }

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

    public SingleLiveEvent<Void> getRequestPermCmd() {
        return mRequestPermCmd;
    }

    public SingleLiveEvent<Void> getCheckPerm() {
        return mCheckPerm;
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
        if (mSource == Constants.SOURCE_DRAFT) {
            if (isRegisteringImage) {
                mStoreDrafTask.storeDraftImage(context,
                        getImagePickedFormat(), getCroppedImageUri().getValue());
            } else {
                mStoreDrafTask.updateInfoDraft(
                        mObjectSource,
                        ((ShotDraft)mObjectSource).getImageUrl(), ((ShotDraft)mObjectSource).getImageFormat(),
                        getTitle().getValue(), getDescription().getValue(),
                        getTags().getValue(), getEditionMode());
            }
        } else if (mSource == Constants.SOURCE_SHOT) {
            mStoreDrafTask.saveInfoDraft(mObjectSource,
                    ((Shot) mObjectSource).getImageUrl(), null,
                    getTitle().getValue(), getDescription().getValue(),
                    getTags().getValue(), getEditionMode());
        } else if (mSource == Constants.SOURCE_FAB) {
            if (isRegisteringImage) {
                mStoreDrafTask.storeDraftImage(context,
                        getImagePickedFormat(), getCroppedImageUri().getValue());
            }
            else
                mStoreDrafTask.saveInfoDraft(mObjectSource,
                        null, null, getTitle().getValue(), getDescription().getValue(),
                        getTags().getValue(), getEditionMode());
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
    public void onSaveImageSuccess(String uri) {
        if (mSource==Constants.SOURCE_DRAFT) {
            Toast.makeText(mApplication, "edition mode :"+getEditionMode(), Toast.LENGTH_SHORT).show();
            mStoreDrafTask.updateInfoDraft(mObjectSource,
                    uri, getImagePickedFormat(),
                    getTitle().getValue(), getDescription().getValue(),
                    getTags().getValue(), getEditionMode());
        } else {
            mStoreDrafTask.saveInfoDraft(mObjectSource,
                    uri, getImagePickedFormat(), getTitle().getValue(), getDescription().getValue(),
                    getTags().getValue(), getEditionMode());
        }
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

    @Override
    public void onTestGood() {
        Toast.makeText(mApplication, "ontestgood", Toast.LENGTH_SHORT).show();
    }

}
