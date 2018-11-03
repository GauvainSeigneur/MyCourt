package seigneur.gauvain.mycourt.ui.main

import android.arch.lifecycle.ViewModel
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Token
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository
import seigneur.gauvain.mycourt.data.repository.TempDataRepository
import seigneur.gauvain.mycourt.data.repository.TokenRepository
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import timber.log.Timber

class MainViewModel @Inject
constructor() : ViewModel() {

    @Inject
    lateinit var mConnectivityReceiver: ConnectivityReceiver

    @Inject
    lateinit var mTempDataRepository: TempDataRepository

    @Inject
    lateinit var mShotDraftRepository: ShotDraftRepository

    @Inject
    lateinit var mTokenRepository: TokenRepository

    private val isInternetLost = false

    private val mCompositeDisposable = CompositeDisposable()

    private var mBottomNavPos = 0
    //Navigation Events
    /*
    *********************************************************************************************
    * EVENT WHICH ACTIVITY WILL SUBSCRIBE
    *********************************************************************************************/
    val navItemSelectedEvent = SingleLiveEvent<Int>()
    val navItemreselectedEvent = SingleLiveEvent<Int>()
    private val mBackNavSystemCommand = SingleLiveEvent<Int>()
    val finishCommand = SingleLiveEvent<Void>()
    val editCommand = SingleLiveEvent<Void>()

    //Results Events
    private val mPublishedCommand = SingleLiveEvent<Void>()
    private val mDraftedCommand = SingleLiveEvent<Void>()

    public override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

    fun getbackNavSystemCommand(): SingleLiveEvent<Int> {
        return mBackNavSystemCommand
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN ACTIVITY
     *********************************************************************************************/
    fun init() {
        Timber.d("mBottomNavPos$mBottomNavPos")
    }

    fun setmBottomNavPos(mBottomNavPos: Int) {
        this.mBottomNavPos = mBottomNavPos
    }

    fun onBottomNavItemSelected() {
        if (mBottomNavPos != -1)
            navItemSelectedEvent.value = mBottomNavPos
    }

    fun onBottomNavItemReselected() {
        if (mBottomNavPos != -1)
            navItemreselectedEvent.value = mBottomNavPos
    }

    fun onAddFabclicked() {
        mTempDataRepository.draftCallingSource = Constants.SOURCE_FAB
        editCommand.call()
    }


    fun onReturnShotDrafted() {
        mDraftedCommand.call()
    }


    fun onReturnShotPublished() {
        mPublishedCommand.call()
        //mMainview.showMessageShotPublished(); //TODO SINGLE EVENT
    }

    fun onReturnNavigation() {
        if (mBottomNavPos != -1 && mBottomNavPos > 0) {
            setmBottomNavPos(mBottomNavPos - 1)
            mBackNavSystemCommand.setValue(mBottomNavPos)
        } else
            finishCommand.call()
    }

    fun checkIfTokenIsNull() {
            fetchTokenFromDB()
    }

    /*
    *********************************************************************************************
    * PRIVATE METHODS
    *********************************************************************************************/
    /**
    * Fetch token from DB - Maybe operator
    */
    private fun fetchTokenFromDB() {
        mCompositeDisposable.add(mTokenRepository.accessTokenFromDB
                .subscribe(
                        this::onTokenFetched,
                        this::onFetchTokenFromDBError,
                        this::onNoTokenFoundInDB)
        )
    }

    /**
     * Token being fetched from DB
     * @param token - Token object
     */
    private fun onTokenFetched(token:Token) {
        Timber.d("token found")
        TokenRepository.accessToken = (token.accessToken).toString()
    }

    /**
     * An error happened during the operation
     * @param throwable - error
     */
    private fun onFetchTokenFromDBError(throwable:Throwable) {
        Timber.d(throwable)
    }

    /**
     * An error happened during the operation
     */
    private fun onNoTokenFoundInDB() {
        //todo -  perform again a Request to get the token again
    }


}
