package seigneur.gauvain.mycourt.ui.shots

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

import javax.inject.Inject

import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.repository.ShotRepository
import seigneur.gauvain.mycourt.data.repository.TempDataRepository
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState
import seigneur.gauvain.mycourt.ui.shots.list.data.datasource.ShotDataSourceFactory
import seigneur.gauvain.mycourt.ui.shots.list.data.datasource.ShotsDataSource
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler
import timber.log.Timber


class ShotsViewModel @Inject
constructor() : ViewModel() {

    @Inject
    lateinit var mNetworkErrorHandler: NetworkErrorHandler

    @Inject
    lateinit var mConnectivityReceiver: ConnectivityReceiver

    @Inject
    lateinit var mTempDataRepository: TempDataRepository

    @Inject
    lateinit var mShotRepository: ShotRepository

    var shotList: LiveData<PagedList<Shot>>? = null

    private val compositeDisposable = CompositeDisposable()

    private val shotDataSourceFactory: ShotDataSourceFactory by lazy {
        ShotDataSourceFactory(compositeDisposable, mShotRepository)
    }

    private var config: PagedList.Config? = null

    val shotClickEvent = SingleLiveEvent<Int>()

    val networkState: LiveData<NetworkState>
        get() =  Transformations.switchMap(shotDataSourceFactory.usersDataSourceLiveData)
        { it.networkState }

    val refreshState: LiveData<NetworkState>
        get() = Transformations.switchMap(shotDataSourceFactory.usersDataSourceLiveData) {
            Timber.d("refresh called ")
            it.initialLoad
        }


    fun init() {
        if (config == null && shotList == null) {
            config = PagedList.Config.Builder()
                    .setPageSize(pageSize)
                    .setInitialLoadSizeHint(pageSize)
                    .setEnablePlaceholders(false)
                    .build()
            shotList = LivePagedListBuilder(shotDataSourceFactory, config!!).build()
        }

    }

    fun retry() {
        if (shotDataSourceFactory.usersDataSourceLiveData.value != null)
            shotDataSourceFactory.usersDataSourceLiveData.value!!.retry()
    }

    fun refresh() {
        if (shotDataSourceFactory.usersDataSourceLiveData.value != null)
            shotDataSourceFactory.usersDataSourceLiveData.value!!.invalidate()
    }

    fun onShotClicked(shot: Shot, position: Int) {
        shotClickEvent.value = position
        mTempDataRepository.shot = shot
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {

        private val pageSize = 15
    }


}
