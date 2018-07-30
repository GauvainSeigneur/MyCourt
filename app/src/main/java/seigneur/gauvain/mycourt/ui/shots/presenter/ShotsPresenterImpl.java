package seigneur.gauvain.mycourt.ui.shots.presenter;

import java.util.List;
import javax.inject.Inject;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import seigneur.gauvain.mycourt.ui.shots.view.ShotsView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import timber.log.Timber;

@PerFragment
public class ShotsPresenterImpl implements ShotsPresenter {

    @Inject
    NetworkErrorHandler mNetworkErrorHandler;

    @Inject
    ShotsView mShotsView;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    TempDataRepository mTempDataRepository;

    @Inject
    ShotRepository mShotRepository;

    //Observer
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    //pagination
    private boolean isLoading = false;
    private boolean isReachedLastPage = false;

    //to compare prev list fetch and next list fetched in loadNextPage();
    private List<Shot> oldList = null;
    private int responsCacheDelay =0;
    //private boolean isAllowedToUpload; //todo - check list first and check user after : if list is empty, check if user is allowed tu upload...

    @Inject
    public ShotsPresenterImpl() {
    }

    @Override
    public void onAttach() {

    }

    @Override
    public void onDetach() {
        compositeDisposable.dispose();
        mShotsView =null;
    }

    @Override
    public void onLoadFirstPage(int page) {
        loadFirstPage(page);
    }

    @Override
    public void onLoadNextPage(int page) {
        loadNextPage(page);
    }

    @Override
    public void onLoadRefresh(int page) {
        loadRefresh(page);
    }

    @Override
    public boolean isLastPageReached() {
        return isReachedLastPage;
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public void onLoading() {
        isLoading=true;
    }

    @Override
    public void onShotClicked(Shot shot, int position) {
        if(mShotsView !=null) {
            mTempDataRepository.setShot(shot); //store the current shot clicked in a dedicated file
            mShotsView.goToShotDetail(shot, position);
            Timber.d(shot.id) ;
        }
    }

    /**************************************************************************
     * first load of data
     *************************************************************************/
    private void loadFirstPage(int page) {
        Timber.d("loadFirstPage: ");
        isReachedLastPage = false;
        // To ensure list is visible when retry button in error view is clicked
        if (mShotsView!=null) {
            mShotsView.showFirstFecthErrorView(false);
            mShotsView.showEndListReached(false,"");
        }
        compositeDisposable.add(
                mShotRepository.getShotsFromAPI(0, page,
                        Constants.PER_PAGE+1) //+1 in order to not finish with one item empty
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                shots -> {
                                    doOnFirstPageNext(shots);
                                },
                                error -> {
                                    handleRetrofitError(error);
                                    doOnFirstPageError(error);
                                }
                        )
        );

    }

    /**
     * Manage success result
     * @param shots - list of shots received by Dribbble
     */
    private void doOnFirstPageNext(List<Shot> shots) {
        oldList =shots;
        Timber.d("list added first time");
        if (mShotsView!=null) {
            //progressBar.setVisibility(View.GONE); //todo
            mShotsView.showFirstFecthErrorView(false);
            mShotsView.addShots(shots);
        }
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnFirstPageError(Throwable error) {
        if (mShotsView!=null) {
            mShotsView.stopRefreshing();
            mShotsView.showFirstFecthErrorView(true);
        }
    }

    /**************************************************************************
     * Fetch another data on scroll
     *************************************************************************/
    private void loadNextPage(int page) {
        Timber.tag("newrequest").d("load next page: "+page);
        isLoading = true;
        isReachedLastPage = false;
        if (mShotsView!=null) {
            mShotsView.showFirstFecthErrorView(false);
            mShotsView.showEndListReached(false,"");
        }
        compositeDisposable.add(
                mShotRepository.getShotsFromAPI(responsCacheDelay, page, Constants.PER_PAGE)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                shots -> {
                                    doOnNextPageNext(shots);
                                    },
                                error -> {
                                    handleRetrofitError(error);
                                    doOnNextPageError(error);
                                }
                                )
        );
    }

    /**
     * Manage success result for next page fetching
     * @param shots - list of shots received by Dribbble
     */
    private void doOnNextPageNext(List<Shot> shots) {
        isLoading = false; //not loading anymore by default
        if (oldList==null) oldList=shots; //if old list was not initiate by first fetch, set it
        //Check if list size received is below the 30 items,
        // if true, we have reached the end : stop request
        if (shots.size()<Constants.PER_PAGE) {
            isReachedLastPage =true; //stop request on scroll
            if (mShotsView!=null) {
                mShotsView.addShots(shots);
                mShotsView.showEndListReached(true, "end");
            }
        } else {
            //if the list size equals 30 items, check the first id,
            // because the server can send the same list
            // if true, we have reached the end : stop request
            if (oldList.get(0).getId().equals(shots.get(0).getId())){
                isReachedLastPage = true; //stop request on scroll
                if (mShotsView!=null) {
                    mShotsView.showEndListReached(true, "end");
                }
                //if false, we didn't reach the end : continue request
            } else {
                oldList=shots;
                isReachedLastPage =false; //continue request on scroll
                if (mShotsView!=null) {
                    mShotsView.addShots(shots);
                }
            }
        }
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnNextPageError(Throwable error) {
        if (mShotsView!=null)
            mShotsView.showNextFetchError(true, error.toString()); //--> todo : mange it in handleRetrofitError
    }

    /**************************************************************************
     * Refresh data in RecyclerView (swipe refresh pull)
     *************************************************************************/
    private void loadRefresh(int page) {
        isReachedLastPage = false;
        compositeDisposable.add(
                mShotRepository.getShotsFromAPI(0, page,
                        Constants.PER_PAGE+1) //+1 in order to not finish with one item empty
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<List<Shot>>() {
                            @Override
                            public void accept(List<Shot> shots) throws Exception {
                                doOnRefreshNext(shots);
                            }
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable error) throws Exception {
                                handleRetrofitError(error);
                                doOnRefreshError(error);
                            }

                        })

                        .subscribe()
        );
    }

    /**
     * Manage success result
     * @param shots - list of shots received by Dribbble
     */
    private void doOnRefreshNext(List<Shot> shots) {
        oldList =shots;
        Timber.d("list refreshing");
        if (mShotsView!=null) {
            mShotsView.showFirstFecthErrorView(false);
            mShotsView.stopRefreshing();
            mShotsView.clearShots(); // todo - use diffutils in order to not use clear all the list
            mShotsView.addShots(shots); // todo - use diffutils in order
            //mShotsView.showLoadingFooter(true);
        }
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnRefreshError(Throwable error) {
        if (mShotsView!=null) {
            mShotsView.stopRefreshing();
            mShotsView.showFirstFecthErrorView(true);
        }
    }

    /**************************************************************************
     * Manage Retrofit error
     * Implementation of NetworkErrorHandler.onRXErrorListener()
     *************************************************************************/
    private void handleRetrofitError(final Throwable error) {
        mNetworkErrorHandler.handleNetworkErrors(error,new NetworkErrorHandler.onRXErrorListener() {
            @Override
            public void onUnexpectedException(Throwable throwable) {
                Timber.d("unexpected error happened, don't know what to do...");
            }

            @Override
            public void onNetworkException(Throwable throwable) {
                Timber.d(throwable);
                if (mConnectivityReceiver.isOnline()) {
                  Timber.d("it seems that you have unexpected errors");
                } else {
                    Timber.d("Not connected to internet, so it is normal that you have an error");
                }

            }

            @Override
            public void onHttpException(Throwable throwable) {
                Timber.tag("HttpNetworks").d(throwable);
                if (((HttpException) throwable).code() == 403) {
                    //todo - access forbidden - wrong credentials may be, check token ! and if user is not prospect or else...
                }
            }
        });
    }

}
