package seigneur.gauvain.mycourt.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.User
import seigneur.gauvain.mycourt.data.repository.UserRepository
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor
import timber.log.Timber

class UserViewModel @Inject
constructor() : ViewModel() {

    @Inject
    lateinit var mConnectivityReceiver: ConnectivityReceiver

    @Inject
    lateinit var mUserRepository: UserRepository

    private val mCompositeDisposable = CompositeDisposable()
    private val userMutableLiveData = MutableLiveData<User>()
    private val mShowSourceCommand = SingleLiveEvent<Void>()

    private var isFetchFromAPI = false
    private val isUserDirty = false


    /**
     *
     * @return
     */
    val user: LiveData<User>
        get() = userMutableLiveData

    public override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN Fragment
     *********************************************************************************************/
    fun init() {
        //Current user is at least fetch from API, so don't request it again unless a refresh
        // is called from user (isUserDirty)
        Timber.d("isfetchFromapi?$isFetchFromAPI")
        if (isFetchFromAPI && user.value != null) {
            Timber.d("do not fetch user again")
            return
        }
        Timber.d("fetch user ")
        fetchUser()

    }

    /**
     * get user from both source and display info
     */
    private fun fetchUser() {
        mCompositeDisposable.add(
                mUserRepository.getUser(false)
                        .subscribe(
                                this::onUserFound, //User found - display info
                                this::onErrorHappened, //Error happened during the request
                                this::manageUIFromDataSource   //Manage UI according to data source
                        )
        )
    }

    /**
     * Display user information according to:
     * data source,
     * available data,
     * user profile
     * @param user object
     */
    private fun onUserFound(user: User) {
        Timber.d("user found " + user.name)
        userMutableLiveData.value = user
    }

    /**
     * An error happened during the request, warn the user
     * @param t - Throwable
     */
    private fun onErrorHappened(t: Throwable) {
        Timber.d(t)
    }

    /**
     * display a message to user according to fetch result
     * and connectivity
     */
    private fun manageUIFromDataSource() {
        if (!mUserRepository.isFetchFromDBSuccess && !mUserRepository.isFetchFromAPISuccess) {
            isFetchFromAPI = false
            //mUserView.showNoUserFoundView(true); //todo - replace
        } else if (!mUserRepository.isFetchFromDBSuccess) {
            Timber.d("user fetch from api only")
            isFetchFromAPI = true
        } else if (!mUserRepository.isFetchFromAPISuccess) {
            isFetchFromAPI = false
            Timber.d("user fetch from DB only")
        } else {
            isFetchFromAPI = true
            Timber.d("user from both source")
        }
    }

}
