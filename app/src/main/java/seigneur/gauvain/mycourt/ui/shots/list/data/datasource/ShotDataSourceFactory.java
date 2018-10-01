package seigneur.gauvain.mycourt.ui.shots.list.data.datasource;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;

/**
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
public class ShotDataSourceFactory extends DataSource.Factory<Long, Shot> {

    private CompositeDisposable compositeDisposable;
    private ShotRepository mShotRepository;

    private MutableLiveData<ShotsDataSource> usersDataSourceLiveData = new MutableLiveData<>();

    public ShotDataSourceFactory(CompositeDisposable compositeDisposable, ShotRepository shotRepository) {
        this.compositeDisposable = compositeDisposable;
        this.mShotRepository =shotRepository;
    }

    @Override
    public DataSource<Long, Shot> create() {
        ShotsDataSource shotsDataSource = new ShotsDataSource(compositeDisposable, mShotRepository);
        usersDataSourceLiveData.postValue(shotsDataSource);
        return shotsDataSource;
    }

    @NonNull
    public MutableLiveData<ShotsDataSource> getUsersDataSourceLiveData() {
        return usersDataSourceLiveData;
    }

}
