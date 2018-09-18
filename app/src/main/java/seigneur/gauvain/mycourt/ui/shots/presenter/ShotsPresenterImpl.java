package seigneur.gauvain.mycourt.ui.shots.presenter;

import java.util.List;
import javax.inject.Inject;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.base.mvp.BasePresenterImpl;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import seigneur.gauvain.mycourt.ui.shots.view.ShotsView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import timber.log.Timber;

@PerFragment
public class ShotsPresenterImpl<V extends ShotsView> extends BasePresenterImpl<V> implements
        ShotsPresenter<V> {

    @Inject
    NetworkErrorHandler mNetworkErrorHandler;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    TempDataRepository mTempDataRepository;

    @Inject
    ShotRepository mShotRepository;

    //pagination
    private boolean isLoading = false;
    private boolean isReachedLastPage = false;

    //to compare prev list fetch and next list fetched in loadNextPage();
    private List<Shot> oldList = null;
    private int responsCacheDelay =0;
    //todo - check list first and check user after : if list is empty, check if user is allowed tu upload...
    //private boolean isAllowedToUpload;

    @Inject
    public ShotsPresenterImpl() {
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
        if(getMvpView() !=null) {
            mTempDataRepository.setShot(shot); //store the current shot clicked in a dedicated file
            getMvpView() .goToShotDetail(shot, position);
            Timber.d(shot.id) ;
        }
    }
    /**************************************************************************
     * First load of data
     *************************************************************************/
    private void loadFirstPage(int page) {
        Timber.d("loadFirstPage: ");
        isReachedLastPage = false;
        // To ensure list is visible when retry button in error view is clicked
        if (getMvpView() !=null) {
            getMvpView().showFirstFecthErrorView(false);
            getMvpView().showEndListReached(false);
        }
        getCompositeDisposable().add(
                mShotRepository.getShotsFromAPI(0, page,
                        Constants.PER_PAGE+1) //+1 in order to not finish with one item empty
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::doOnFirstPageNext,
                                error -> {
                                    handleRetrofitError(error,-1);
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
        if (getMvpView()!=null) {
            //progressBar.setVisibility(View.GONE); //todo
            getMvpView().showFirstFecthErrorView(false);
            getMvpView().addShots(shots);
        }
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnFirstPageError(Throwable error) {
        if (getMvpView()!=null) {
            getMvpView().stopRefreshing();
            getMvpView().showFirstFecthErrorView(true);
        }
    }

    /**************************************************************************
     * Fetch another data on scroll
     *************************************************************************/
    private void loadNextPage(int page) {
        Timber.tag("newrequest").d("load next page: "+page);
        isLoading = true;
        isReachedLastPage = false;
        if (getMvpView()!=null) {
            //todo - reorganize in dedicated void
            getMvpView().showFirstFecthErrorView(false);
            getMvpView().showEndListReached(false);
            getMvpView().showNextFetchError(false, null);
        }
        getCompositeDisposable().add(
                mShotRepository.getShotsFromAPI(responsCacheDelay, page, Constants.PER_PAGE)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::doOnNextPageNext,
                                error -> {
                                    handleRetrofitError(error,-1);
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
            if (getMvpView()!=null) {
                getMvpView().addShots(shots);
                getMvpView().showEndListReached(true);
            }
        } else {
            //if the list size equals 30 items, check the first id,
            // because the server can send the same list
            // if true, we have reached the end : stop request
            if (oldList.get(0).getId().equals(shots.get(0).getId())){
                isReachedLastPage = true; //stop request on scroll
                if (getMvpView()!=null) {
                    getMvpView().showEndListReached(true);
                }
                //if false, we didn't reach the end : continue request
            } else {
                oldList=shots;
                isReachedLastPage =false; //continue request on scroll
                if (getMvpView()!=null) {
                    getMvpView().addShots(shots);
                }
            }
        }
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnNextPageError(Throwable error) {
        if (getMvpView()!=null)
            getMvpView().showNextFetchError(true, error.toString()); //--> todo : mange it in handleRetrofitError
    }

    /**************************************************************************
     * Refresh data in RecyclerView (swipe refresh pull)
     *************************************************************************/
    private void loadRefresh(int page) {
        isReachedLastPage = false;
        getCompositeDisposable().add(
                mShotRepository.getShotsFromAPI(0, page,
                        Constants.PER_PAGE+1) //+1 in order to not finish with one item empty
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::doOnRefreshNext, //success
                                error -> {
                                    handleRetrofitError(error,-1);
                                    doOnRefreshError(error);
                                }
                        )

        );
    }

    /**
     * Manage success result
     * @param shots - list of shots received by Dribbble
     */
    private void doOnRefreshNext(List<Shot> shots) {
        oldList =shots;
        Timber.d("list refreshing");
        if (getMvpView()!=null) {
            getMvpView().showFirstFecthErrorView(false);
            getMvpView().stopRefreshing();
            getMvpView().clearShots(); // todo - use diffutils in order to not use clear all the list
            getMvpView().addShots(shots); // todo - use diffutils in order
            //mShotsView.showLoadingFooter(true);
        }
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnRefreshError(Throwable error) {
        if (getMvpView()!=null) {
            getMvpView().stopRefreshing();
            //todo - only if the list is already empty!
            getMvpView().showFirstFecthErrorView(true);
        }
    }

    /**************************************************************************
     * Manage Retrofit error
     * Implementation of NetworkErrorHandler.onRXErrorListener()
     *************************************************************************/
    private void handleRetrofitError(final Throwable error, int eventID) {
        mNetworkErrorHandler.handleNetworkErrors(error, eventID, new NetworkErrorHandler.onRXErrorListener() {
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
