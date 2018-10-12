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
     * Save Draft in DB
     * @param draft - draft created at the beginning of activity
     */
    public void save(Draft draft) {
        mCompositeDisposable.add(
                mShotDraftRepository.storeShotDraft(draft)
                        .subscribe(
                                this::onDraftSaved, //todo Listener
                                this::onDraftSavingError //todo Listener
                        )
        );

    }


    /**
     * Update draft in db
     * @param draft - draft created at the beginning of activity
     */
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

    /**
     * CALLBACK FOR VIEWMODEL
     */
    public interface StoreRequestListener {

        void onSaveImageSuccess(String uri);

        void onStoreDraftSucceed();

        void onFailed();
    }


}
