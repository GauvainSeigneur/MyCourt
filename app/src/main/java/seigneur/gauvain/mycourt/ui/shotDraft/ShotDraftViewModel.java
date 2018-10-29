package seigneur.gauvain.mycourt.ui.shotDraft;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import timber.log.Timber;

public class ShotDraftViewModel extends ViewModel {

    @Inject
    ShotDraftRepository mShotDraftRepository;

    @Inject
    TempDataRepository mTempDataRepository;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private final SingleLiveEvent<Void> mStopRefreshEvent = new SingleLiveEvent<>();

    private final SingleLiveEvent<Draft> mItemClicked = new SingleLiveEvent<>();

    private final MutableLiveData<List<Draft>> mShotDrafts = new MutableLiveData<>();

    private boolean isRefreshing;

    @Inject
    public ShotDraftViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        mCompositeDisposable.clear();
    }

    public LiveData<List<Draft>> getDrafts() {
        return mShotDrafts;
    }

    public SingleLiveEvent<Void> dbChanged() {
        return mShotDraftRepository.onDraftDBChanged;
    }

    public SingleLiveEvent<Draft> getItemClickedEvent() {
        return mItemClicked;
    }


    public void fetchShotDrafts() {
        isRefreshing=false;
        mCompositeDisposable.add(fetchDrafts()
                .subscribe(
                        this::doOnDraftFound,
                        this::doOnError,
                        this::doOnNothingFound
                )
        );
    }

    public void onRefresh(boolean fromSwipeRefresh) {
        if (fromSwipeRefresh) {
            isRefreshing=true;
            mCompositeDisposable.add(fetchDrafts()
                    .subscribe(
                            this::doOnDraftFound,
                            this::doOnError,
                            this::doOnNothingFound));
        } else {
            Timber.d("not refreshing, nothing happened");
        }
    }

    /**
     * get ShotDrafts list from DB - Use mayBe because the list will be small
     * @return - List of ShotDraft
     */
    private Maybe<List<Draft>> fetchDrafts() {
        Timber.d ("getPostFromDB called");
        return mShotDraftRepository.getShotDraft();
    }

    /**
     * ShotDrafts being found in DB - do something with it
     * @param shotDrafts - list Found in DB
     */
    private void doOnDraftFound(List<Draft> shotDrafts){
        Timber.d("list loaded"+shotDrafts.toString());
        //todo - live data
        mShotDrafts.setValue(shotDrafts);

        /*if (!shotDrafts.isEmpty()) {
        } else {

        }*/

    }

    /**
     * When nothing found in DB, stop refreshing and set up a dedicated view
     */
    private void doOnNothingFound(){
        if (isRefreshing) {
            mStopRefreshEvent.call();
            //mShotDraftView.stopRefresh(); //SINGLE EVENT ?
        }

    }

    /**
     * Error happened during shotDraft fetching
     * @param throwable - error
     */
    private void doOnError(Throwable throwable){
        Timber.e(throwable);
        //TODO -SINGLE EVENT?
    }


    public void onShotDraftClicked(Draft shotDraft, int position) {
        mTempDataRepository.setDraftCallingSource(Constants.SOURCE_DRAFT);
        mTempDataRepository.setShotDraft(shotDraft);
        mItemClicked.setValue(shotDraft);

        // mShotDraftView.goToShotEdition(); //TODO -SINGLE EVENT

    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN PRESENTER
     *********************************************************************************************/


}
