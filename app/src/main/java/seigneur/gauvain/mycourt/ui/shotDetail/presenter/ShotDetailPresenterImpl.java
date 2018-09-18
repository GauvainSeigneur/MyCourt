package seigneur.gauvain.mycourt.ui.shotDetail.presenter;

import android.graphics.drawable.Drawable;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.base.BasePresenterImplTest;
import seigneur.gauvain.mycourt.ui.pin.presenter.PinPresenter;
import seigneur.gauvain.mycourt.ui.pin.view.PinView;
import seigneur.gauvain.mycourt.ui.shotDetail.view.ShotDetailView;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

@PerActivity
public class ShotDetailPresenterImpl<V extends ShotDetailView> extends BasePresenterImplTest<V> implements
        ShotDetailPresenter<V> {

    @Inject
    ShotDetailView mShotDetailView;

    @Inject
    TempDataRepository mTempDataRepository;

    @Inject
    ShotDraftRepository mShotDraftRepository;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Shot mShot;

    @Inject
    public ShotDetailPresenterImpl() {
    }

    @Override
    public void onViewReady() {
        getShot();
    }

    @Override
    public void onEditShotClicked() {
        if (mShotDetailView!=null) {
            checkDraftAndGoToEdition();
        }

    }

    @Override
    public void onShotImageAvailable(boolean isResourceReady, Drawable resource) {
        if (mShotDetailView!=null) {
            mShotDetailView.startPosponedEnterTransition();
            mShotDetailView.showPaletteShot(isResourceReady);
            if (isResourceReady)
                mShotDetailView.adaptColorToShot(resource);
            mShotDetailView.initImageScrollBehavior();
            mShotDetailView.setUpShotInfo(mShot);
        }
    }

    @Override
    public void OnReturnFromEdition(int result) {
        if (mShotDetailView!=null) {
            mShotDetailView.showEditionResult(result);
        }
    }

    /*
     **************************************************************************
     * Get Shot clicked
     *************************************************************************/
    private void getShot() {
        compositeDisposable.add(Single.just(mTempDataRepository.getShot())
                .subscribe(
                        this::doOnShotRetrieve, //success
                        this::doOnShotError //error
                )
        );
    }

    /**
     * Manage when shot is retrieved
     * @param shot - shot retrieve from TempRepository
     */
    private void doOnShotRetrieve(Shot shot) {
        mShot=shot;
        if (mShotDetailView!=null) {
            mShotDetailView.loadShotImage(shot);
        }
    }

    /**
     * Manage error
     * @param error - Throwable
     */
    private void doOnShotError(Throwable error) {
        if (mShotDetailView!=null) {
            //todo - do something better
            mShotDetailView.showErrorView(true);
        }
    }

    /**************************************************************************
     * Check if the shot has already a draft saved in DB. if it has,
     * call its draft, if not, just go to Edition
     *************************************************************************/
    private void checkDraftAndGoToEdition() {
        compositeDisposable.add(
                mShotDraftRepository.getShotDraftByShotId(mShot.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::doIfDraftFoundInDB, //maybe onNext: shot draft found
                                t -> Timber.e(t), //error
                                this::doIfNoDraftFoundInDB //maybe onComplete without onNext called : nothing found
                        ));
    }

    /**
     * Manage if ShotDraft is found in db
     * @param shotDraft - shotDRaft found in db
     */
    private void doIfDraftFoundInDB(ShotDraft shotDraft) {
        Timber.d("Draft already exists");
        mTempDataRepository.setDraftCallingSource(Constants.SOURCE_DRAFT);
        mTempDataRepository.setShotDraft(shotDraft);
        if (mShotDetailView!=null)
            mShotDetailView.goToShotEdition();
    }

    /**
     * Manage if no ShotDraft found in db
     */
    private void doIfNoDraftFoundInDB() {
        Timber.d("no item found");
        mTempDataRepository.setDraftCallingSource(Constants.SOURCE_SHOT);
        mTempDataRepository.setShot(mShot);
        //mTempDataRepository.setEditionMode(Constants.EDIT_MODE_UPDATE_SHOT);
        if (mShotDetailView!=null)
            mShotDetailView.goToShotEdition();
    }


}
