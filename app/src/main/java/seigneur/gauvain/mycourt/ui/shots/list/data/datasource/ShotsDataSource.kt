package seigneur.gauvain.mycourt.ui.shots.list.data.datasource

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.ItemKeyedDataSource
import android.arch.paging.PageKeyedDataSource


import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import seigneur.gauvain.mycourt.data.api.DribbbleService
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.data.repository.ShotRepository
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState
import timber.log.Timber


class ShotsDataSource

    internal constructor(private val compositeDisposable: CompositeDisposable,
                         private val mShotRepository: ShotRepository) : PageKeyedDataSource<Long, Shot>() {


    /*
    * Step 1: Initialize the restApiFactory.
    * The networkState and initialLoading variables
    * are for updating the UI when data is being fetched
    * by displaying a progress bar
    */

    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    /**
     * Keep Completable reference for the retry event
     */
    private var retryCompletable: Completable? = null

    fun retry() {
        if (retryCompletable != null) {
            compositeDisposable.add(retryCompletable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ }, { throwable -> Timber.e(throwable.message) }))
        }
    }

    /*
     * Step 2: This method is responsible to load the data initially
     * when app screen is launched for the first time.
     * We are fetching the first page data from the api
     * and passing it via the callback method to the UI.
     */
    override fun loadInitial(params: PageKeyedDataSource.LoadInitialParams<Long>, callback: PageKeyedDataSource.LoadInitialCallback<Long, Shot>) {
        Timber.d("loadInitial called")
        // update network states.
        // we also provide an initial load state to the listeners so that the UI can know when the
        // very first list is loaded.
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        //get the initial shots from the api
        compositeDisposable.add(
                mShotRepository.getShotsFromAPItest(0, 1, params.requestedLoadSize)
                        .subscribe(
                                { shots ->
                                    // clear retry since last request succeeded
                                    setRetry(null)
                                    networkState.postValue(NetworkState.LOADED)
                                    initialLoad.postValue(NetworkState.LOADED)
                                    callback.onResult(shots, null, 2L)
                                },
                                { throwable ->
                                    // keep a Completable for future retry
                                    setRetry(Action { loadInitial(params, callback) })
                                    val error = NetworkState.error(throwable.message)
                                    // publish the error
                                    networkState.postValue(error)
                                    initialLoad.postValue(error)
                                }
                        )
        )

    }


    override fun loadBefore(params: PageKeyedDataSource.LoadParams<Long>, callback: PageKeyedDataSource.LoadCallback<Long, Shot>) {
        // ignored, since we only ever append to our initial load
    }

    /*
     * Step 3: This method it is responsible for the subsequent call to load the data page wise.
     * This method is executed in the background thread
     * We are fetching the next page data from the api
     * and passing it via the callback method to the UI.
     * The "params.key" variable will have the updated value.
     */
    override fun loadAfter(params: PageKeyedDataSource.LoadParams<Long>, callback: PageKeyedDataSource.LoadCallback<Long, Shot>) {
        // set network value to loading.
        Timber.d("loadAfter called")
        networkState.postValue(NetworkState.LOADING)

        compositeDisposable.add(mShotRepository.getShotsFromAPItest(0, params.key, params.requestedLoadSize)
                .subscribe(
                        { shots ->
                            //long nextKey = (params.key == shots.body().getTotalResults()) ? null : params.key+1; //TODO - to reactivate
                            val nextKey = params.key + 1
                            // clear retry since last request succeeded
                            setRetry(null)
                            networkState.postValue(NetworkState.LOADED)
                            callback.onResult(shots, nextKey)
                        },
                        { throwable ->
                            // keep a Completable for future retry
                            setRetry(Action { loadAfter(params, callback) })
                            // publish the error
                            networkState.postValue(NetworkState.error(throwable.message))
                        }
                )
        )

    }

    private fun setRetry(action: Action?) {
        if (action == null) {
            this.retryCompletable = null
        } else {
            this.retryCompletable = Completable.fromAction(action)
        }
    }

}
