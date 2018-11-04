package seigneur.gauvain.mycourt.ui.shots.list.data.datasource

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource

import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.repository.ShotRepository

/**
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class ShotDataSourceFactory(
        private val compositeDisposable: CompositeDisposable,
        private val mShotRepository: ShotRepository) : DataSource.Factory<Long, Shot>() {

    val usersDataSourceLiveData = MutableLiveData<ShotsDataSource>()

    override fun create(): DataSource<Long, Shot> {
        val shotsDataSource = ShotsDataSource(compositeDisposable, mShotRepository)
        usersDataSourceLiveData.postValue(shotsDataSource)
        return shotsDataSource
    }

}
