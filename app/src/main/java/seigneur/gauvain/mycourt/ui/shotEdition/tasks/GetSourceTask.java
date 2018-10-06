package seigneur.gauvain.mycourt.ui.shotEdition.tasks;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

public class GetSourceTask {

    private SourceCallback mSourceCallback;

    public GetSourceTask(SourceCallback sourceCallback) {
        this.mSourceCallback=sourceCallback;
    }

    /*
    *********************************************************************************************
    * Manage source
    *********************************************************************************************/
    // Check whether if the activity is opened from draft registered in database or other
    public void getOriginOfEditRequest(CompositeDisposable compositeDisposable,
                                        TempDataRepository tempDataRepository) {
        compositeDisposable
                .add(Single.just(tempDataRepository.getDraftCallingSource())
                        .subscribe(
                                source -> manageSource(compositeDisposable, tempDataRepository, source),
                                this::manageSourceTypeError)
                );
    }

    private void manageSource(CompositeDisposable compositeDisposable,
                              TempDataRepository tempDataRepository,
                              int source) {
        mSourceCallback.source(source);
        switch (source) {
            //user wishes to continue edit a stored draft
            case Constants.SOURCE_DRAFT:
                getShotDraft(compositeDisposable,tempDataRepository); //edition mode stored in draft, get draft to know edition mode
                break;
            //User wishes to update a published shot
            case Constants.SOURCE_SHOT:
                mSourceCallback.EditMode(Constants.EDIT_MODE_UPDATE_SHOT);
                getShot(compositeDisposable,tempDataRepository);
                break;
            //User wishes to create a shot
            case Constants.SOURCE_FAB:
                mSourceCallback.EditMode(Constants.EDIT_MODE_NEW_SHOT);
                mSourceCallback.dataForUIReady();
                break;
        }
    }

    private void manageSourceTypeError(Throwable throwable) {
        Timber.d(throwable);
        mSourceCallback.source(-1);
    }

    /*
    *********************************************************************************************
    * Get shot object if source is Constants.SOURCE_SHOT
    *********************************************************************************************/
    private void getShot(CompositeDisposable compositeDisposable,TempDataRepository tempDataRepository) {
        compositeDisposable.add(Single.just(tempDataRepository.getShot())
                .subscribe(this::manageShotInfo, this::onGetShotError));
    }

    private void manageShotInfo(Shot shot) {
        mSourceCallback.objectSource(shot);
        mSourceCallback.dataForUIReady();
    }

    private void onGetShotError(Throwable throwable) {
        Timber.d(throwable);
    }

    /*
    *********************************************************************************************
    * Get ShotDraft object if source is Constants.SOURCE_SHOT
    *********************************************************************************************/
    private void getShotDraft(CompositeDisposable compositeDisposable,TempDataRepository tempDataRepository) {
        compositeDisposable.add(Single.just(tempDataRepository.getShotDraft())
                .subscribe(
                        this::manageShotDraftInfo,
                        this::onGetShotDraftError
                )
        );
    }

    private void manageShotDraftInfo(ShotDraft shotDraft) {
        mSourceCallback.objectSource(shotDraft);
        mSourceCallback.EditMode(shotDraft.getDraftType());
        mSourceCallback.dataForUIReady();
    }

    private void onGetShotDraftError(Throwable throwable) {
        Timber.d(throwable);
    }


    public interface SourceCallback {

        void source(int source);

        void EditMode(int mode);

        void dataForUIReady();

        void objectSource(Object object);

    }


}
