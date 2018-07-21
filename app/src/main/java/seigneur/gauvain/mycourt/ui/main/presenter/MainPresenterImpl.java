package seigneur.gauvain.mycourt.ui.main.presenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.data.repository.TokenRepository;
import seigneur.gauvain.mycourt.ui.main.view.MainView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

public class MainPresenterImpl implements MainPresenter {

    @Inject
    MainView mMainview;

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

    @Override
    public void onAttach() {
    }
    @Override
    public void onDetach() {
        compositeDisposable.dispose();
        mMainview=null;
    }

    @Override
    public void onBottomNavItemSelected(int position) {
        mMainview.showFragment(position);
    }

    @Override
    public void onBottomNavItemReselected(int position) {
        //here - manage back on top, refresh ?
        mMainview.goBackAtStart(position);
    }

    @Override
    public void onAddFabclicked() {
        mTempDataRepository.setDraftCallingSource(Constants.SOURCE_FAB);
        mMainview.goToShotEdition();
    }

    @Override
    public void onReturnShotDrafted() {
       mMainview.showMessageShotDrafted();
    }

    @Override
    public void onReturnShotPublished() {
        mMainview.showMessageShotPublished();
    }

    @Override
    public void onCheckInternetConnection() {
        checkInternetConnection();
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
    public void onReturnFromDraftPublishing() {
        //todo - update shotDraft fragment
    }

    @Override
    public void checkIfTokenIsNull() {
        if(TokenRepository.accessToken==null)
            fetchTokenFromDB();
    }

    private void fetchTokenFromDB() {
        compositeDisposable.add(mTokenRepository.getAccessTokenFromDB()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        token -> {
                            if (mMainview!=null) {
                                Timber.d("token found");
                                TokenRepository.accessToken = String.valueOf(token.getAccessToken());
                            }
                        },
                        c-> {
                            Timber.d("no token found");
                            //todo - avert user that the app has lost his token, he must reconnect his account
                        }
                )
        );
    }




}
