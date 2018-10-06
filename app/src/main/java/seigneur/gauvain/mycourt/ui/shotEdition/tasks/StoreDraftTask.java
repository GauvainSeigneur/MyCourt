package seigneur.gauvain.mycourt.ui.shotEdition.tasks;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

public class StoreDraftTask {
    //TODO - concat storeDraft image and SaveDraft in DB

   private StoreRequestListener mStoreRequestListener;

    @Inject
    public StoreDraftTask(StoreRequestListener requestListener) {
        this.mStoreRequestListener=requestListener;
    }

    /**
     * Store image in "My Court" folder in external storage and get the URI to save it in DB
     * @param context - context, must be application context
     * @param compositeDisposable
     * @param shotDraftRepository
     * @param imageCroppedFormat
     * @param croppedFileUri
     * @param imageStorageURL
     * @param id
     * @param title
     * @param desc
     * @param profile
     * @param tags
     * @param typeOfDraft
     * @param dateOfPublication
     * @param dateOfUpdate
     */
    public void storeDraftImage(Context context,
                                CompositeDisposable compositeDisposable,
                                ShotDraftRepository shotDraftRepository,
                                String imageCroppedFormat,
                                Uri croppedFileUri,
                                String imageStorageURL,
                                String id,
                                String title,
                                String desc,
                                boolean profile,
                                ArrayList<String> tags,
                                int typeOfDraft,
                                @Nullable Date dateOfPublication,
                                @Nullable Date dateOfUpdate) {
        compositeDisposable.add(shotDraftRepository.storeImageAndReturnItsUri(imageCroppedFormat,croppedFileUri,context)
                .onErrorResumeNext(t -> t instanceof NullPointerException ? Single.error(t):Single.error(t)) //todo : to comment this
                .subscribe(
                        //TODO -listener !!!
                        uri -> saveDraft(compositeDisposable,
                                shotDraftRepository,
                                imageStorageURL,
                                imageCroppedFormat,
                                id,
                                title,
                                desc,
                                profile,
                                tags,
                                typeOfDraft,
                                dateOfPublication,
                                dateOfUpdate),
                        this::doOnCopyImageError
                )
        );
    }

    /**
     * Save or update draft in db
     * @param compositeDisposable
     * @param shotDraftRepository
     * @param imageUri
     * @param imageFormat
     * @param id
     * @param title
     * @param desc
     * @param profile
     * @param tags
     * @param typeOfDraft
     * @param dateOfPublication
     * @param dateOfUpdate
     */
    public void saveDraft(CompositeDisposable compositeDisposable,
                            ShotDraftRepository shotDraftRepository,
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
        Timber.d("save draft called");
            //just update a draft
            ShotDraft shotDraft =  createShotDraft(0,imageUri,imageFormat,id,title,desc, profile, tags, typeOfDraft, dateOfPublication, dateOfUpdate);
            compositeDisposable.add(
                    shotDraftRepository.storeShotDraft(shotDraft)
                            .subscribe(
                                    this::onDraftSaved, //todo Listener
                                    this::onDraftSavingError //todo Listener
                            )
            );

    }

    public void updateDraft(
            CompositeDisposable compositeDisposable,
            ShotDraftRepository shotDraftRepository,
            Object sourceObject,
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
        Timber.d("update draft called");
        ShotDraft shotDraft =  createShotDraft(((ShotDraft)sourceObject).getId(),imageUri,imageFormat,id,title,desc, profile, tags, typeOfDraft, dateOfPublication, dateOfUpdate);
        compositeDisposable.add(
                shotDraftRepository.updateShotDraft(shotDraft)
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
     * CALLBACK FOR VIEWMODEL
     */
    public interface StoreRequestListener {

        void onSaveImageSuccess();

        void onStoreDraftSucees();

        void onUpdateDraftSuccess();

        void onFailed();
    }


}
