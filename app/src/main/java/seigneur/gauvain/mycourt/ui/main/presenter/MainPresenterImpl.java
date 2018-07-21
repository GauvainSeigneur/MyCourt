package seigneur.gauvain.mycourt.ui.main.presenter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.ui.main.view.MainView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;

public class MainPresenterImpl implements MainPresenter {

    @Inject
    MainView mMainview;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    TempDataRepository mTempDataRepository;

   private boolean isInternetLost=false;

   Disposable disposable = new CompositeDisposable();

    @Inject
    public MainPresenterImpl(){}

    @Override
    public void onAttach() {
    }
    @Override
    public void onDetach() {
        disposable.dispose();
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




}
