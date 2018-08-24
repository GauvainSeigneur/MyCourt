package seigneur.gauvain.mycourt.ui.shotDraft.presenter;

import java.util.List;
import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.shotDraft.view.ShotDraftView;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

@PerFragment
public class ShotDraftPresenterImpl implements ShotDraftPresenter {

    @Inject
    ShotDraftView mShotDraftView;

    @Inject
    ShotDraftRepository mShotDraftRepository;

    @Inject
    TempDataRepository mTempDataRepository;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    private boolean isRefreshing;

    @Inject
    public ShotDraftPresenterImpl() {
    }

    @Override
    public void onAttach() {
        isRefreshing=false;
        compositeDisposable.add(getShotDrafts()
                .subscribe(
                      this::doOnDraftFound,
                      this::doOnError,
                      this::doOnNothingFound
                )
        );
    }

    @Override
    public void onDetach() {
        compositeDisposable.dispose();
        mShotDraftView =null;
    }

    @Override
    public void onRefresh(boolean fromSwipeRefresh) {
        if (fromSwipeRefresh || mTempDataRepository.isDraftsChanged()) {
            isRefreshing=true;
            compositeDisposable.add(getShotDrafts()
                    .subscribe(
                            this::doOnDraftFound,
                            this::doOnError,
                            this::doOnNothingFound));
        } else {
            Timber.d("not refreshing, nothing happened");
        }
    }

    @Override
    public void onShotDraftClicked(ShotDraft shotDraft, int position) {
        if (mShotDraftView!=null) {
            mTempDataRepository.setDraftCallingSource(Constants.SOURCE_DRAFT);
            mTempDataRepository.setShotDraft(shotDraft);
            mShotDraftView.goToShotEdition();
        }
    }

    /**
     * get ShotDrafts list from DB - Use mayBe because the list will be small
     * @return - List of ShotDraft
     */
    private Maybe<List<ShotDraft>> getShotDrafts() {
        Timber.d ("getPostFromDB called");
        return mShotDraftRepository.getShotDraft();
    }

    /**
     * ShotDrafts being found in DB - do something with it
     * @param shotDrafts - list Found in DB
     */
    private void doOnDraftFound(List<ShotDraft> shotDrafts){
        mTempDataRepository.setDraftsChanged(false); //Consume the event
        Timber.d("list loaded"+shotDrafts.toString());
        if (mShotDraftView!=null) {
            mShotDraftView.stopRefresh();
            if (!shotDrafts.isEmpty()) {
                mShotDraftView.showEmptyView(false);
                mShotDraftView.showDraftList(shotDrafts, isRefreshing);
            } else {
                mShotDraftView.showEmptyView(true);
            }
        }
    }

    /**
     * When nothing found in DB, stop refreshing and set up a dedicated view
     */
    private void doOnNothingFound(){
        mTempDataRepository.setDraftsChanged(false);
        if (mShotDraftView!=null && isRefreshing)
            mShotDraftView.stopRefresh();
    }

    /**
     * Error happened during shotDraft fetching
     * @param throwable - error
     */
    private void doOnError(Throwable throwable){
        Timber.e(throwable);
        if (mShotDraftView!=null)
            mShotDraftView.stopRefresh();
    }

}

