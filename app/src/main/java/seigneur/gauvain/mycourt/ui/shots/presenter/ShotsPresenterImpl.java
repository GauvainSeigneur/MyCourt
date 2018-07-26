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

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    Throwable mThrowable;

    //pagination tests
    private boolean isLoading = false;
    private boolean isReachedLastPage = false;
    //to compare prev list fetch and next list fetched in loadNextPage();
    private List<Shot> oldList = null;
   // private List<Shot> newList =null;
    private int responsCacheDelay =0;
    private boolean isAllowedToUpload =true;

    @Inject
    public ShotsPresenterImpl() {
    }


    @Override
    public void onAttach() {
        //todo - here check if user is at least prospect!
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
    public boolean onLastPageReached() {
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

    private void loadFirstPage(int page) {
        Timber.d("loadFirstPage: ");
        isReachedLastPage = false;
        // To ensure list is visible when retry button in error view is clicked
        if (mShotsView!=null) {
            mShotsView.showFirstFecthErrorView(false);
        }
        compositeDisposable.add(
                mShotRepository.getShotsFromAPI(0, page, Constants.PER_PAGE)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<List<Shot>>() {
                            @Override
                            public void accept(List<Shot> shots) throws Exception {
                                oldList =shots;
                                Timber.d("list added first time");
                                if (mShotsView!=null) {
                                    //progressBar.setVisibility(View.GONE); //todo
                                    mShotsView.showFirstFecthErrorView(false);
                                    mShotsView.addShots(shots);
                                }

                            }
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable error) throws Exception {
                                handleRetrofitError(error);
                                //showErrorView(error); --> todo : reactivatre in handleRetrofitError
                            }

                        })

                        .subscribe()
        );

    }

    //todo : réfléchir a ctte solution: si la liste reçue est inférieure à 30 (défaut par DRibble) ou
    //la quantité par page définie par moi, alors ne pas appeler on loadNextPage
    private void loadNextPage(int page) {
        Timber.d("load next page: "+page);
        compositeDisposable.add(
                mShotRepository.getShotsFromAPI(responsCacheDelay, page, Constants.PER_PAGE)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<List<Shot>>() {
                            @Override
                            public void accept(List<Shot> shots) throws Exception {
                                isLoading = false;
                                if (oldList==null)
                                    oldList=shots;
                                if (shots.size()<Constants.PER_PAGE) {
                                    Timber.tag("newrequest").d("not the same size");
                                    isReachedLastPage =true; //stop request on scroll
                                    if (mShotsView!=null) {
                                        //mShotsView.showLoadingFooter(false);
                                        mShotsView.showEndListMessage(true);
                                        mShotsView.addShots(shots);
                                    }
                                } else {
                                    if (oldList.get(0).getId().equals(shots.get(0).getId())){
                                        Timber.tag("newrequest").d("same id");
                                        isReachedLastPage =true; //continue request on scroll
                                        if (mShotsView!=null) {
                                            mShotsView.showEndListMessage(true);
                                            //mShotsView.showLoadingFooter(false);
                                        }
                                    } else {
                                        Timber.tag("newrequest").d("not same id");
                                        oldList=shots;
                                        isReachedLastPage =false; //continue request on scroll
                                        if (mShotsView!=null) {
                                            mShotsView.addShots(shots);
                                        }
                                    }
                                }
                            }
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable error) throws Exception {
                                handleRetrofitError(error);
                                if (mShotsView!=null)
                                    mShotsView.showNextFetchError(true, error.toString()); //--> todo : mange it in handleRetrofitError
                            }

                        })
                        .subscribe()
        );
    }

    /**
     * add a delay between two request maybe
     * @param page
     */
    private void loadRefresh(int page) {
        isReachedLastPage = false;
        compositeDisposable.add(
                mShotRepository.getShotsFromAPI(0, page,Constants.PER_PAGE)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<List<Shot>>() {
                            @Override
                            public void accept(List<Shot> shots) throws Exception {
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
                        })
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable error) throws Exception {
                                handleRetrofitError(error);
                                if (mShotsView!=null) {
                                    mShotsView.stopRefreshing();
                                    mShotsView.showFirstFecthErrorView(true);
                                }
                                //showErrorView(error); --> todo : reactivatre in handleRetrofitError
                            }

                        })

                        .subscribe()
        );

    }

    //todo - better management of this !
    private void handleRetrofitError(final Throwable error) {
        mNetworkErrorHandler.handleNetworkErrors(error,new NetworkErrorHandler.onRXErrorListener() {
            @Override
            public void onUnexpectedException(Throwable throwable) {
                Timber.d("unexpected error happened, don't know what to do...");
                mThrowable = throwable;
            }

            @Override
            public void onNetworkException(Throwable throwable) {
                Timber.d(throwable);
                mThrowable = throwable;
                if (mConnectivityReceiver.isOnline()) {
                  Timber.d("it seems that you have unexpected errors");
                } else {
                    Timber.d("Not connected to internet, so it is normal that you have an error");
                }

            }

            @Override
            public void onHttpException(Throwable throwable) {
                Timber.tag("HttpNetworks").d(throwable);
                mThrowable = throwable;
                if (((HttpException) throwable).code() == 403) {
                    //todo - access forbidden
                }
            }



        });
    }

}
