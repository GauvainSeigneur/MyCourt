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
    public void onRefresh() {
        isRefreshing=true;
        compositeDisposable.add(getShotDrafts()
                .subscribe(
                        this::doOnDraftFound,
                        this::doOnError,
                        this::doOnNothingFound));
    }

    @Override
    public void onShotDraftClicked(ShotDraft shotDraft, int position) {
        if (mShotDraftView!=null) {
            mTempDataRepository.setDraftCallingSource(Constants.SOURCE_DRAFT);
            mTempDataRepository.setShotDraft(shotDraft);
            mShotDraftView.goToShotEdition();
        }
    }


    private Maybe<List<ShotDraft>> getShotDrafts() {
        Timber.d ("getPostFromDB called");
        return mShotDraftRepository.getShotDraft()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void doOnDraftFound(List<ShotDraft> shotDrafts){
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

    private void doOnNothingFound(){
        if (mShotDraftView!=null && isRefreshing)
            mShotDraftView.stopRefresh();
    }

    private void doOnError(Throwable throwable){
        Timber.e(throwable);
        if (mShotDraftView!=null)
            mShotDraftView.stopRefresh();
    }

}

