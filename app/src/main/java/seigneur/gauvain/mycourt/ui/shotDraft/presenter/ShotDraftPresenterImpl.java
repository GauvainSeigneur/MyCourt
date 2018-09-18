package seigneur.gauvain.mycourt.ui.shotDraft.presenter;

import java.util.List;
import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.base.mvp.BasePresenterImpl;
import seigneur.gauvain.mycourt.ui.shotDraft.view.ShotDraftView;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

@PerFragment
public class ShotDraftPresenterImpl<V extends ShotDraftView> extends BasePresenterImpl<V> implements
        ShotDraftPresenter<V> {

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
    public void onViewReady() {
        isRefreshing=false;
        compositeDisposable.add(getShotDrafts()
                .subscribe(
                      this::doOnDraftFound,
                      this::doOnError,
                      this::doOnNothingFound
                )
        );
    }

    /*@Override
    public void onDetach() {
        compositeDisposable.dispose();
        mShotDraftView =null;
    }*/

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
        if (getMvpView()!=null) {
            mTempDataRepository.setDraftCallingSource(Constants.SOURCE_DRAFT);
            mTempDataRepository.setShotDraft(shotDraft);
            getMvpView().goToShotEdition();
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
        if (getMvpView()!=null) {
            getMvpView().stopRefresh();
            if (!shotDrafts.isEmpty()) {
                getMvpView().showEmptyView(false);
                getMvpView().showDraftList(shotDrafts, isRefreshing);
            } else {
                getMvpView().showEmptyView(true);
            }
        }
    }

    /**
     * When nothing found in DB, stop refreshing and set up a dedicated view
     */
    private void doOnNothingFound(){
        mTempDataRepository.setDraftsChanged(false);
        if (getMvpView()!=null && isRefreshing)
            getMvpView().stopRefresh();
    }

    /**
     * Error happened during shotDraft fetching
     * @param throwable - error
     */
    private void doOnError(Throwable throwable){
        Timber.e(throwable);
        if (getMvpView()!=null)
            getMvpView().stopRefresh();
    }

}

