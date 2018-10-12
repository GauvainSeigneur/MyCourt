package seigneur.gauvain.mycourt.ui.shotEdition.tasks;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.model.Token;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

public class StoreDraftTask {

    private CompositeDisposable     mCompositeDisposable;
    private StoreRequestListener    mStoreRequestListener;
    private ShotDraftRepository     mShotDraftRepository;
    private Shot mShot;


    @Inject
    public StoreDraftTask(CompositeDisposable compositeDisposable,
                          ShotDraftRepository shotDraftRepository,
                          StoreRequestListener requestListener) {
        this.mCompositeDisposable=compositeDisposable;
        this.mShotDraftRepository=shotDraftRepository;
        this.mStoreRequestListener=requestListener;
    }

    /**
     * Store cropped image in external storage and get Uri of the this file to save it in DB
     * @param context - use Application context
     * @param imageCroppedFormat - manage cropping according to the format
     * @param croppedFileUri -  uri of the image after being cropped
     */
    public void storeDraftImage(Context context,
                                String imageCroppedFormat,
                                Uri croppedFileUri) {
        mCompositeDisposable.add(
                mShotDraftRepository.storeImageAndReturnItsUri(imageCroppedFormat,croppedFileUri,context)
                .onErrorResumeNext(t -> t instanceof NullPointerException ?
                        Single.error(t):Single.error(t)) //todo : to comment this
                .subscribe(
                        uri -> mStoreRequestListener.onSaveImageSuccess(uri),
                        this::onDraftSavingError
                )
        );
    }

    /**
     * Save draft in db
     * @param sourceObject - can be Shot or ShotDraft
     * @param imageUri - uri of the image (on Dribbble.com/from My Court folder/or null)
     * @param imageFormat - can be jpeg, png, gif or null
     * @param title - title of shot
     * @param desc - description of the shot
     * @param tags - list of tags of the shot
     * @param typeOfDraft - can be NEW_SHOT_DRAFT OR UPDATE
     */
    public void saveInfoDraft(Object sourceObject, @Nullable String imageUri, @Nullable String imageFormat,
                              String title, String desc, ArrayList<String> tags, int typeOfDraft) {


        Timber.d("save draft called");
            if (sourceObject instanceof Shot) {
                mShot = (Shot) sourceObject;
                mShot.setTitle(title);
                mShot.setDescription(desc);
                mShot.setTagList(tags);
            } else {
                 mShot = new Shot(null, title,desc, tags);
            }
            Draft draft = createDraft(typeOfDraft,imageUri, imageFormat, null, mShot);
            Timber.d("draft created: "+draft.shot.getTitle());
            mCompositeDisposable.add(
                        mShotDraftRepository.storeShotDraft(draft)
                                .subscribe(
                                        this::onDraftSaved,
                                        this::onDraftSavingError
                                )
                );
    }

    public void save(Draft draft) {
        mCompositeDisposable.add(
                mShotDraftRepository.storeShotDraft(draft)
                        .subscribe(
                                this::onDraftSaved, //todo Listener
                                this::onDraftSavingError //todo Listener
                        )
        );

    }


    public void update(Draft draft) {
        mCompositeDisposable.add(
                mShotDraftRepository.updateShotDraft(draft)
                        .subscribe(
                                this::onDraftSaved, //todo Listener
                                this::onDraftSavingError //todo Listener
                        )
        );

    }

    /**
     * Update a current draft in DB
     */
    public void updateInfoDraft(Object sourceObject, @Nullable String imageUri,
                                @Nullable String imageFormat, String title, String desc,
                                ArrayList<String> tags, int typeOfDraft) {

        Draft draft = (Draft) sourceObject;
        draft.setImageUri(imageUri);
        draft.setImageFormat(imageFormat);
        draft.shot.setTitle(title);
        draft.shot.setDescription(desc);
        draft.shot.setTagList(tags);

        mCompositeDisposable.add(
                mShotDraftRepository.updateShotDraft(draft)
                        .subscribe(
                                this::onDraftSaved, //todo Listener
                                this::onDraftSavingError //todo Listener
                        )
        );

    }

    /**
     * Draft has been saved/updated in DB
     */
    public void onDraftSaved() {
        mStoreRequestListener.onStoreDraftSucceed();
        Timber.d("draft saved");
    }

    /**
     * An error occurred while trying to saved draft in db
     * @param t - throwable
     */
    public void onDraftSavingError(Throwable t) {
        Timber.d(t);
        mStoreRequestListener.onFailed();
    }

    /*
    *********************************************************************************************
    * CREATE DRAFT OBJECT
    *********************************************************************************************/
    /**
     * Create a draft object to store it in DB
     * @param typeOfDraft
     * @param imageUri
     * @param imageFormat
     * @param schedulingDate
     * @param shot
     * @return
     */
    public Draft createDraft(
            int typeOfDraft,
            @Nullable String imageUri,
            @Nullable String imageFormat,
            @Nullable  Date schedulingDate,
            @Nullable Shot shot //todo - can relly be Nullable?
            ) {
        return new Draft(typeOfDraft, imageUri, imageFormat, schedulingDate, shot);
    }


    /**
     * CALLBACK FOR VIEWMODEL
     */
    public interface StoreRequestListener {

        void onSaveImageSuccess(String uri);

        void onStoreDraftSucceed();

        void onFailed();
    }


}
