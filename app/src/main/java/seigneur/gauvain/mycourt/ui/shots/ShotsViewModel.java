package seigneur.gauvain.mycourt.ui.shots;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.ui.shots.view.ShotsView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

public class ShotsViewModel extends ViewModel {

    //todo - use paging library : https://proandroiddev.com/8-steps-to-implement-paging-library-in-android-d02500f7fffe
    //todo : this to : https://proandroiddev.com/exploring-paging-library-from-jetpack-c661c7399662

    @Inject
    NetworkErrorHandler mNetworkErrorHandler;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    TempDataRepository mTempDataRepository;

    @Inject
    ShotRepository mShotRepository;

    //Observer
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    //pagination
    //private boolean isLoading = false;
    //private boolean isReachedLastPage = false;
    //private MutableLiveData<Boolean> isLastPage = new MutableLiveData<>();
    private MutableLiveData<List<Shot>> mShotListFetched = new MutableLiveData<>();
    private MutableLiveData<Integer> mListFooterState = new MutableLiveData<>();

    //to compare prev list fetch and next list fetched in loadNextPage();
    private List<Shot> oldList = null;
    private int responsCacheDelay =0;
    private int mPage = 1;

    @Inject
    public ShotsViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
    }


    public LiveData<List<Shot>> geShotsFetched() {
        return mShotListFetched;
    }


    public LiveData<Integer> getListFooterState() {
        if (mListFooterState.getValue()==null)
            mListFooterState.setValue(Constants.FOOTER_STATE_LOADING);

        return mListFooterState;
    }

    /**************************************************************************
     * Methods called in Fragment
     *************************************************************************/

    public void onLoadFirstPage() {
        if (mShotListFetched.getValue()!=null)
            return;
        loadFirstPage(mPage);
    }

    public void onLoadNextPage() {
        loadNextPage(mPage);
    }

    public void onLoadRefresh() {
        loadRefresh(mPage);
    }

    /*public boolean isLastPageReached() {
        return isReachedLastPage;
    }*/

    public void onShotClicked(Shot shot, int position) {
        mTempDataRepository.setShot(shot); //store the current shot clicked in a dedicated file
        //todo - singleEVENT

        /*if(mShotsView !=null) {
            mTempDataRepository.setShot(shot); //store the current shot clicked in a dedicated file
            mShotsView.goToShotDetail(shot, position);
            Timber.d(shot.id) ;
        }*/
    }

    /**************************************************************************
     * First load of data
     *************************************************************************/
    private void loadFirstPage(int page) {
        Timber.d("loadFirstPage: ");
        compositeDisposable.add(
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
        Timber.d("list added first time");
        oldList = shots;
        mPage++;
        mShotListFetched.setValue(shots);
        mListFooterState.setValue(Constants.FOOTER_STATE_NOT_LOADING);
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnFirstPageError(Throwable error) {
        //todo - LIVE DATA
        mListFooterState.setValue(Constants.FOOTER_STATE_ERROR);
        /*if (mShotsView!=null) {
            mShotsView.stopRefreshing();
            mShotsView.showFirstFecthErrorView(true);
        }*/
    }

    /**************************************************************************
     * Fetch another data on scroll
     *************************************************************************/
    private void loadNextPage(int page) {
        Timber.tag("newrequest").d("load next page: "+page);
        //todo - LIVE DATA /SINGLE EVENT
        mListFooterState.setValue(Constants.FOOTER_STATE_LOADING);
        //todo - LIVE DATA /SINGLE EVENT
        /*if (mShotsView!=null) {
            mShotsView.showFirstFecthErrorView(false);
            mShotsView.showEndListReached(false);
            mShotsView.showNextFetchError(false, null);
        }*/

        compositeDisposable.add(
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
        Timber.d("onNextpage called");
        mPage++;
        mListFooterState.setValue(Constants.FOOTER_STATE_NOT_LOADING);
        if (oldList==null)
            oldList=shots; //if old list was not initiate by first fetch, set it
        //Check if list size received is below the 30 items,
        // if true, we have reached the end : stop request
        if (shots.size()<Constants.PER_PAGE) {
            Timber.d("end of list");
            mListFooterState.setValue(Constants.FOOTER_STATE_END);
            mShotListFetched.setValue(shots);
        } else {
            //if the list size equals 30 items, check the first id,
            // because the server can send the same list
            // if true, we have reached the end : stop request
            if (oldList.get(0).getId().equals(shots.get(0).getId())){
                Timber.d("end of list");
                mListFooterState.setValue(Constants.FOOTER_STATE_END);
                //if false, we didn't reach the end : continue request
            } else {
                oldList=shots;
                mShotListFetched.setValue(shots);
            }
        }
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnNextPageError(Throwable error) {
        //todo - LIVE DATA  /SINGLE EVENT ?
        mListFooterState.setValue(Constants.FOOTER_STATE_ERROR);
        /*if (mShotsView!=null)
            mShotsView.showNextFetchError(true, error.toString());
            */
             //--> todo : mange it in handleRetrofitError
    }

    /**************************************************************************
     * Refresh data in RecyclerView (swipe refresh pull)
     *************************************************************************/
    private void loadRefresh(int page) {
        //isReachedLastPage = false;
        //isLastPage.setValue(false);
        mListFooterState.setValue(Constants.FOOTER_STATE_LOADING);
        compositeDisposable.add(
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
        mPage=0;
        oldList =shots;
        mShotListFetched.setValue(shots);
        Timber.d("list refreshing");
        //todo - LIVE DATA

       /* if (mShotsView!=null) {
            mShotsView.showFirstFecthErrorView(false);
            mShotsView.stopRefreshing();
            mShotsView.clearShots(); // todo - use diffutils in order to not use clear all the list
            mShotsView.addShots(shots); // todo - use diffutils in order
            //mShotsView.showLoadingFooter(true);
        }*/
    }

    /**
     * Manage error result
     * @param error - error received by Observer
     */
    private void doOnRefreshError(Throwable error) {

        //todo - LIVE DATA
        /*/if (mShotsView!=null) {
            mShotsView.stopRefreshing();
            //todo - only if the list is already empty!
            mShotsView.showFirstFecthErrorView(true);
        }
        */
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
