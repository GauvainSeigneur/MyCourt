package seigneur.gauvain.mycourt.ui.main;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.view.MenuItem;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Token;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.data.repository.TokenRepository;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import timber.log.Timber;

public class MainViewModel extends ViewModel {

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    TempDataRepository mTempDataRepository;

    @Inject
    ShotDraftRepository mShotDraftRepository;

    @Inject
    TokenRepository mTokenRepository;

    private boolean isInternetLost=false;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private int mBottomNavPos = 0;
    //Navigation Events
    private final SingleLiveEvent<Integer> mNavItemSelected = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> mNavItemReselected= new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> mBackNavSystemCommand= new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> mFinishCommand = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> mEditCommand = new SingleLiveEvent<>();

    //Results Events
    private final SingleLiveEvent<Void> mPublishedCommand = new SingleLiveEvent<>();
    private final SingleLiveEvent<Void> mDraftedCommand = new SingleLiveEvent<>();


    @Inject
    public MainViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        mCompositeDisposable.clear();
    }

    /*
     *********************************************************************************************
     * EVENT WHICH ACTIVITY WILL SUBSCRIBE
     *********************************************************************************************/
    public SingleLiveEvent<Integer> getNavItemSelectedEvent() {
        return mNavItemSelected;
    }

    public SingleLiveEvent<Integer> getNavItemreselectedEvent() {
        return mNavItemReselected;
    }

    public SingleLiveEvent<Integer> getbackNavSystemCommand() {
        return mBackNavSystemCommand;
    }

    public SingleLiveEvent<Void> getFinishCommand() {
        return mFinishCommand;
    }

    public SingleLiveEvent<Void> getEditCommand() {
        return mEditCommand;
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN ACTIVITY
     *********************************************************************************************/
    public void init() {
        Timber.d("mBottomNavPos"+mBottomNavPos);
    }

    public void setmBottomNavPos(int mBottomNavPos) {
        this.mBottomNavPos = mBottomNavPos;
    }

    public void onBottomNavItemSelected() {
        if (mBottomNavPos != -1)
            mNavItemSelected.setValue(mBottomNavPos);
    }

    public void onBottomNavItemReselected() {
        if (mBottomNavPos != -1)
            mNavItemReselected.setValue(mBottomNavPos);
    }

    public void onAddFabclicked() {
        mTempDataRepository.setDraftCallingSource(Constants.SOURCE_FAB);
        mEditCommand.call();
    }


    public void onReturnShotDrafted() {
        mDraftedCommand.call();
    }


    public void onReturnShotPublished() {
        mPublishedCommand.call();
        //mMainview.showMessageShotPublished(); //TODO SINGLE EVENT
    }

    public void onReturnNavigation() {
        if (mBottomNavPos!=-1 && mBottomNavPos>0) {
            setmBottomNavPos(mBottomNavPos-1);
            mBackNavSystemCommand.setValue(mBottomNavPos);
        }
        else
            mFinishCommand.call();
    }

    public void checkIfTokenIsNull() {
        if(TokenRepository.accessToken==null)
            fetchTokenFromDB();
    }

   /*
  *********************************************************************************************
  * PRIVATE METHODS
  *********************************************************************************************

    /**
    * Fetch token from DB - Maybe operator
    */
    private void fetchTokenFromDB() {
        mCompositeDisposable.add(mTokenRepository.getAccessTokenFromDB()
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
        Timber.d("token found");
        TokenRepository.accessToken = String.valueOf(token.getAccessToken());
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
