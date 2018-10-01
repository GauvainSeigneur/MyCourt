package seigneur.gauvain.mycourt.ui.shots.list.data.datasource;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.ItemKeyedDataSource;
import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;



import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.api.DribbbleService;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState;
import timber.log.Timber;

public class ShotsDataSource extends PageKeyedDataSource<Long, Shot> {

    /*
     * Step 1: Initialize the restApiFactory.
     * The networkState and initialLoading variables
     * are for updating the UI when data is being fetched
     * by displaying a progress bar
     */
    private ShotRepository mShotRepository;

    private CompositeDisposable compositeDisposable;

    private MutableLiveData<NetworkState> networkState = new MutableLiveData<>();

    private MutableLiveData<NetworkState> initialLoad = new MutableLiveData<>();

    /**
     * Keep Completable reference for the retry event
     */
    private Completable retryCompletable;

    ShotsDataSource(CompositeDisposable compositeDisposable, ShotRepository shotRepository) {
        this.mShotRepository = shotRepository;
        this.compositeDisposable = compositeDisposable;
    }

    public void retry() {
        if (retryCompletable != null) {
            compositeDisposable.add(retryCompletable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                    }, throwable -> Timber.e(throwable.getMessage())));
        }
    }

    /*
     * Step 2: This method is responsible to load the data initially
     * when app screen is launched for the first time.
     * We are fetching the first page data from the api
     * and passing it via the callback method to the UI.
     */
    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Long, Shot> callback) {
        Timber.d("loadInitial called");
        // update network states.
        // we also provide an initial load state to the listeners so that the UI can know when the
        // very first list is loaded.
        networkState.postValue(NetworkState.LOADING);
        initialLoad.postValue(NetworkState.LOADING);
        //get the initial shots from the api
        compositeDisposable.add(
                mShotRepository.getShotsFromAPItest(0,1, params.requestedLoadSize)
                .subscribe(
                        shots -> {
                            // clear retry since last request succeeded
                            setRetry(null);
                            networkState.postValue(NetworkState.LOADED);
                            initialLoad.postValue(NetworkState.LOADED);
                            callback.onResult(shots, null, 2l);
                        },
                        throwable -> {
                            // keep a Completable for future retry
                            setRetry(() -> loadInitial(params, callback));
                            NetworkState error = NetworkState.error(throwable.getMessage());
                            // publish the error
                            networkState.postValue(error);
                            initialLoad.postValue(error);
                        }
                )
        );

    }


    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Long, Shot> callback) {
        // ignored, since we only ever append to our initial load
    }

    /*
     * Step 3: This method it is responsible for the subsequent call to load the data page wise.
     * This method is executed in the background thread
     * We are fetching the next page data from the api
     * and passing it via the callback method to the UI.
     * The "params.key" variable will have the updated value.
     */
    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Long, Shot> callback) {
        // set network value to loading.
        Timber.d("loadAfter called");
        networkState.postValue(NetworkState.LOADING);

        compositeDisposable.add(mShotRepository.getShotsFromAPItest(0,params.key, params.requestedLoadSize)
                .subscribe(
                        shots -> {
                            //long nextKey = (params.key == shots.body().getTotalResults()) ? null : params.key+1; //TODO - to reactivate
                            long nextKey = params.key+1;
                            // clear retry since last request succeeded
                            setRetry(null);
                            networkState.postValue(NetworkState.LOADED);
                            callback.onResult(shots, nextKey);
                        },
                        throwable -> {
                            // keep a Completable for future retry
                            setRetry(() -> loadAfter(params, callback));
                            // publish the error
                            networkState.postValue(NetworkState.error(throwable.getMessage()));
                        }
                )
        );

    }

    @NonNull
    public MutableLiveData<NetworkState> getNetworkState() {
        return networkState;
    }

    @NonNull
    public MutableLiveData<NetworkState> getInitialLoad() {
        return initialLoad;
    }

    private void setRetry(final Action action) {
        if (action == null) {
            this.retryCompletable = null;
        } else {
            this.retryCompletable = Completable.fromAction(action);
        }
    }

}
