package seigneur.gauvain.mycourt.ui.main.presenter;

import android.view.MenuItem;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Token;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.data.repository.TokenRepository;
import seigneur.gauvain.mycourt.ui.base.mvp.BasePresenterImpl;
import seigneur.gauvain.mycourt.ui.main.view.MainView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

public class MainPresenterImpl<V extends MainView> extends BasePresenterImpl<V> implements
        MainPresenter<V> {

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    TempDataRepository mTempDataRepository;

    @Inject
    ShotDraftRepository mShotDraftRepository;

    @Inject
    TokenRepository mTokenRepository;

   private boolean isInternetLost=false;

   CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public MainPresenterImpl(){}

   /* @Override
    public void onAttach() {
    }

    @Override
    public void onDetach() {
        compositeDisposable.dispose();
        mMainview=null;
    }*/

    @Override
    public void onBottomNavItemSelected(int pos) {
        getMvpView().showFragment(pos);
    }

    @Override
    public void onBottomNavItemReselected(int position) {
        //here - manage back on top, refresh ?
        getMvpView().goBackAtStart(position);
    }

    @Override
    public void onAddFabclicked() {
        mTempDataRepository.setDraftCallingSource(Constants.SOURCE_FAB);
        getMvpView().goToShotEdition();
    }

    @Override
    public void onReturnShotDrafted() {
        getMvpView().showMessageShotDrafted();
    }

    @Override
    public void onReturnShotPublished() {
        getMvpView().showMessageShotPublished();
    }

    @Override
    public void onCheckInternetConnection() {
        checkInternetConnection();
    }

    @Override
    public void onReturnNavigation(MenuItem item, int position) {
        if (item!=null &&position!=-1 && position>0)
            getMvpView().goBackOnPrevItem(position-1);
        else
            getMvpView().closeActivity();
    }

    private void checkInternetConnection() {

      /*if (mConnectivityReceiver.isOnline() && isInternetLost) {
          if(mMainview!=null) {
              mMainview.showNoInternetConnectionMessage(false);
              mMainview.showInternetConnectionRetrieved(true);
          }
          isInternetLost=false;
      } else if (!mConnectivityReceiver.isOnline()) {
          if(mMainview!=null) {
              mMainview.showInternetConnectionRetrieved(false);
              mMainview.showNoInternetConnectionMessage(true);
          }
          isInternetLost=true;
      }*/
    }


    @Override
    public void checkIfTokenIsNull() {
        if(TokenRepository.accessToken==null)
            fetchTokenFromDB();
    }

    /**
     * Fetch token from DB - Maybe operator
     */
    private void fetchTokenFromDB() {
        compositeDisposable.add(mTokenRepository.getAccessTokenFromDB()
                .subscribe(
                        this::onTokenFetched,
                        this:: onFetchTokenFromDBError,
                        this:: onNoTokenFoundInDB
                )
        );
    }

    /**
     * Token being fetched from DB
     * @param token - Token object
     */
    private void onTokenFetched(Token token) {
        if (getMvpView()!=null) {
            Timber.d("token found");
            TokenRepository.accessToken = String.valueOf(token.getAccessToken());
        }
    }

    /**
     * An error happened during the operation
     * @param throwable - error
     */
    private void onFetchTokenFromDBError(Throwable throwable) {
        Timber.d(throwable);
    }

    /**
     * An error happened during the operation
     */
    private void onNoTokenFoundInDB() {
       //todo -  perform again a Request to get the token again
    }



}
