package seigneur.gauvain.mycourt.ui.shots;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState;
import seigneur.gauvain.mycourt.ui.shots.list.data.datasource.ShotDataSourceFactory;
import seigneur.gauvain.mycourt.ui.shots.list.data.datasource.ShotsDataSource;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;


public class ShotsViewModel extends ViewModel {

    @Inject
    NetworkErrorHandler mNetworkErrorHandler;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    TempDataRepository mTempDataRepository;

    @Inject
    ShotRepository mShotRepository;

    LiveData<PagedList<Shot>> shotList;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private static final int pageSize = 15;

    private ShotDataSourceFactory shotDataSourceFactory;

    private PagedList.Config config;

    private SingleLiveEvent<Integer> shotClickEvent = new SingleLiveEvent<>();

    @Inject
    public ShotsViewModel() { }

    public void init() {
        if (shotDataSourceFactory==null && config==null && shotList==null) {
            shotDataSourceFactory = new
                    ShotDataSourceFactory(compositeDisposable, mShotRepository);
            config = new PagedList.Config.Builder()
                    .setPageSize(pageSize)
                    .setInitialLoadSizeHint(pageSize)
                    .setEnablePlaceholders(false)
                    .build();
            shotList = new LivePagedListBuilder<>(shotDataSourceFactory, config).build();
        }

    }

    public void retry() {
        if (shotDataSourceFactory.getUsersDataSourceLiveData().getValue()!=null)
            shotDataSourceFactory.getUsersDataSourceLiveData().getValue().retry();
    }

    public void refresh() {
        if (shotDataSourceFactory.getUsersDataSourceLiveData().getValue()!=null)
            shotDataSourceFactory.getUsersDataSourceLiveData().getValue().invalidate();
    }

    public LiveData<NetworkState> getNetworkState() {
        return Transformations.switchMap(shotDataSourceFactory.getUsersDataSourceLiveData(),
                ShotsDataSource::getNetworkState);
    }

    public LiveData<NetworkState> getRefreshState() {
        return Transformations.switchMap(shotDataSourceFactory.getUsersDataSourceLiveData(),
                ShotsDataSource::getInitialLoad);
    }

    public SingleLiveEvent<Integer> getShotClickEvent() {
        return shotClickEvent;
    }

    public void onShotClicked(Shot shot, int position) {
        shotClickEvent.setValue(position);
        mTempDataRepository.setShot(shot);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }



}
