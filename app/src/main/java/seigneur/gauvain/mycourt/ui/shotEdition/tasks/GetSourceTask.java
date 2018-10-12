package seigneur.gauvain.mycourt.ui.shotEdition.tasks;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

public class GetSourceTask {

    private SourceCallback mSourceCallback;
    private TempDataRepository mTempDataRepository;
    private CompositeDisposable mCompositeDisposable;

    public GetSourceTask(TempDataRepository tempDataRepository,
                         CompositeDisposable compositeDisposable,
                         SourceCallback sourceCallback) {
        this.mTempDataRepository=tempDataRepository;
        this.mCompositeDisposable=compositeDisposable;
        this.mSourceCallback=sourceCallback;
    }

    /*
    *********************************************************************************************
    * Manage source
    *********************************************************************************************/
    // Check whether if the activity is opened from draft registered in database or other
    public void getOriginOfEditRequest() {
        mCompositeDisposable
                .add(Single.just(mTempDataRepository.getDraftCallingSource())
                        .subscribe(
                                source -> manageSource(source),
                                this::manageSourceTypeError)
                );
    }

    private void manageSource(int source) {
        switch (source) {
            //user wishes to continue edit a stored draft
            case Constants.SOURCE_DRAFT:
                getShotDraft(); //edition mode stored in draft, get draft to know edition mode
                break;
            //User wishes to update a published shot
            case Constants.SOURCE_SHOT:
                getShot();
                break;
            //User wishes to create a shot
            case Constants.SOURCE_FAB:
                Shot shot = new Shot();
                Draft draft = new Draft (Constants.EDIT_MODE_NEW_SHOT, null, null, null, shot);
                mSourceCallback.setUpTempDraft(draft);
                mSourceCallback.dataForUIReady();
                break;
        }
    }

    private void manageSourceTypeError(Throwable throwable) {
        Timber.d(throwable);
    }

    /*
    *********************************************************************************************
    * Get shot object if source is Constants.SOURCE_SHOT
    *********************************************************************************************/
    private void getShot() {
        mCompositeDisposable.add(Single.just(mTempDataRepository.getShot())
                .subscribe(this::manageShotInfo, this::onGetShotError));
    }

    private void manageShotInfo(Shot shot) {
        Draft draft = new Draft (Constants.EDIT_MODE_UPDATE_SHOT, shot.getImageHidpi(), null, null, shot);
        mSourceCallback.setUpTempDraft(draft);

        mSourceCallback.dataForUIReady();
    }

    private void onGetShotError(Throwable throwable) {
        Timber.d(throwable);
    }

    /*
    *********************************************************************************************
    * Get ShotDraft object if source is Constants.SOURCE_SHOT
    *********************************************************************************************/
    private void getShotDraft() {
        mCompositeDisposable.add(Single.just(mTempDataRepository.getShotDraft())
                .subscribe(
                        this::manageShotDraftInfo,
                        this::onGetShotDraftError
                )
        );
    }

    private void manageShotDraftInfo(Draft shotDraft) {
        Draft draft = shotDraft;
        mSourceCallback.setUpTempDraft(draft);
        mSourceCallback.dataForUIReady();
        Timber.d("typeofDraft : "+ shotDraft.getTypeOfDraft());
    }

    private void onGetShotDraftError(Throwable throwable) {
        Timber.d(throwable);
    }


    public interface SourceCallback {

        void setUpTempDraft(Draft draft);

        void dataForUIReady();


    }


}
