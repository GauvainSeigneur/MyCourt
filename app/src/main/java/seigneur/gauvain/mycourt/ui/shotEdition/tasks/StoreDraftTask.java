package seigneur.gauvain.mycourt.ui.shotEdition.tasks;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

public class StoreDraftTask {
    //TODO - concat storeDraft image and SaveDraft in DB
    private CompositeDisposable mCompositeDisposable;
    private StoreRequestListener mStoreRequestListener;
    private ShotDraftRepository mShotDraftRepository;

    @Inject
    public StoreDraftTask(CompositeDisposable compositeDisposable,
            ShotDraftRepository shotDraftRepository,
            StoreRequestListener requestListener) {
        this.mCompositeDisposable=compositeDisposable;
        this.mShotDraftRepository=shotDraftRepository;
        this.mStoreRequestListener=requestListener;
    }


    //Store image in "My Court" folder in external storage and get the URI to save it in DB
    public void storeDraftImage(Context context,
                                String imageCroppedFormat,
                                Uri croppedFileUri) {
        mCompositeDisposable.add(
                mShotDraftRepository.storeImageAndReturnItsUri(imageCroppedFormat,croppedFileUri,context)
                .onErrorResumeNext(t -> t instanceof NullPointerException ? Single.error(t):Single.error(t)) //todo : to comment this
                .subscribe(
                        //TODO -listener !!!
                        uri -> mStoreRequestListener.onSaveImageSuccess(uri),
                        this::doOnCopyImageError
                )
        );
    }


    public void saveDraft(Object sourceObject,
                              @Nullable String imageUri,
                              @Nullable String imageFormat,
                              String title,
                              String desc,
                              ArrayList<String> tags,
                              int typeOfDraft) {
        Timber.d("save draft called");
        //just update a draft
        ShotDraft shotDraft =  createShotDraft(
                getDraftId(sourceObject), imageUri,imageFormat,
                getShotId(sourceObject),
                title,desc, getProfile(sourceObject), tags,
                typeOfDraft, getDateOfPublication(sourceObject), getDateOfUpdate(sourceObject));
        mCompositeDisposable.add(
                mShotDraftRepository.storeShotDraft(shotDraft)
                        .subscribe(
                                this::onDraftSaved, //todo Listener
                                this::onDraftSavingError //todo Listener
                        )
        );

    }
    //Save only info of draft
    public void saveInfoDraft(
                          Object sourceObject,
                          @Nullable String imageUri,
                          @Nullable String imageFormat,
                          String title,
                          String desc,
                          ArrayList<String> tags,
                          int typeOfDraft) {
        Timber.d("save draft called");
            //just update a draft
            ShotDraft shotDraft =  createShotDraft(
                    getDraftId(sourceObject), imageUri,imageFormat,
                    getShotId(sourceObject),
                    title,desc, getProfile(sourceObject), tags,
                    typeOfDraft, getDateOfPublication(sourceObject), getDateOfUpdate(sourceObject));
        mCompositeDisposable.add(
                    mShotDraftRepository.storeShotDraft(shotDraft)
                            .subscribe(
                                    this::onDraftSaved, //todo Listener
                                    this::onDraftSavingError //todo Listener
                            )
            );
    }

    //update draft in db
    public void updateInfoDraft(
            Object sourceObject,
            @Nullable String imageUri,
            @Nullable String imageFormat,
            String title,
            String desc,
            ArrayList<String> tags,
            int typeOfDraft) {
        Timber.d("update draft called");
        ShotDraft shotDraft =  createShotDraft(
                getDraftId(sourceObject),
                imageUri,
                imageFormat,
                getShotId(sourceObject),title,desc, getProfile(sourceObject), tags, typeOfDraft,
                getDateOfPublication(sourceObject),
                getDateOfUpdate(sourceObject));
        mCompositeDisposable.add(
                mShotDraftRepository.updateShotDraft(shotDraft)
                        .subscribe(
                                this::onDraftSaved, //todo Listener
                                this::onDraftSavingError //todo Listener
                        )
        );

    }

    /**
     * Indicates to user that an error occurred while trying to copy Image in MyCourt folder
     * @param t - throwable
     */
    public void doOnCopyImageError(Throwable t)  {
        Timber.d(t);
    }

    /**
     * Draft has been saved/updated in DB
     */
    public void onDraftSaved() {
        Timber.d("draft saved");
        //mTempDataRepository.setDraftsChanged(true); //TODO LIVE DATA
        //TODO SINGLE LIVE EVENT
        /*if (mEditShotView!=null)
            mEditShotView.notifyPostSaved();*/
    }

    /**
     * An error occurred while trying to saved draft in db
     * @param t - throwable
     */
    public void onDraftSavingError(Throwable t) {
        Timber.d(t);
    }

    /*
     *********************************************************************************************
     * CREATE DRAFT OBJECT
     *********************************************************************************************/
    /**
     * Create Shot draft object to insert or update a ShotDraft
     * @param primaryKey - unique identifier
     * @param imageUri - image url
     * @param imageFormat _ jpeg, png, gif
     * @return ShotDraft
     */
    public ShotDraft createShotDraft(int primaryKey,
                                      @Nullable String imageUri,
                                      @Nullable String imageFormat,
                                      String id,
                                      String title,
                                      String desc,
                                      boolean profile,
                                      ArrayList<String> tags,
                                      int typeOfDraft,
                                      @Nullable Date dateOfPublication,
                                      @Nullable Date dateOfUpdate) {
        return new ShotDraft(
                primaryKey,
                imageUri,
                imageFormat,
                id,
                title, //todo - is live data here
                desc,
                profile,
                null, //todo - for phase 2 : allow user to schedule publishing
                tags,
                -1, //todo - for phase 2 : manage team
                typeOfDraft,
                dateOfPublication,
                dateOfUpdate
        );
    }

    /**
     *
     * @param objectSource
     * @return
     */
    private int getDraftId(Object objectSource) {
        if (objectSource instanceof Shot) {
            return 0;
        } else if (objectSource instanceof ShotDraft) {
            return ((ShotDraft) objectSource).getId();
        } else {
            return 0;
        }
    }


    /**
     * get shot id
     * @param objectSource
     * @return
     */
    private String getShotId(Object objectSource) {
        if (objectSource instanceof Shot) {
            return ((Shot) objectSource).getId();
        } else if (objectSource instanceof ShotDraft) {
            return ((ShotDraft) objectSource).getShotId();
        } else {
            return "undefined";
        }
    }

    /**
     * get profile of the shot - todo manage it in UI for future
     * @param objectSource
     * @return
     */
    private boolean getProfile(Object objectSource) {
        if (objectSource instanceof Shot) {
            return ((Shot) objectSource).isLow_profile();
        } else if (objectSource instanceof ShotDraft) {
            return ((ShotDraft) objectSource).isLowProfile();
        } else {
            return false;
        }
    }

    /**
     * Get date of publication for this draft
     * @param objectSource
     * @return
     */
    private Date getDateOfPublication(Object objectSource) {
        if (objectSource instanceof Shot) {
            return ((Shot) objectSource).getPublishDate();
        } else if (objectSource instanceof ShotDraft) {
            return ((ShotDraft) objectSource).getDateOfPublication();
        } else {
            return null;
        }
    }

    /**
     *
     * @param objectSource
     * @return
     */
    private Date getDateOfUpdate(Object objectSource) {
        if (objectSource instanceof Shot) {
            return ((Shot) objectSource).getUpdateDate();
        } else if (objectSource instanceof ShotDraft) {
            return ((ShotDraft) objectSource).getDateOfUpdate();
        } else {
            return null;
        }
    }

    /**
     * CALLBACK FOR VIEWMODEL
     */
    public interface StoreRequestListener {

        void onSaveImageSuccess(String uri);

        void onStoreDraftSucees();

        void onUpdateDraftSuccess();

        void onFailed();
    }


}
