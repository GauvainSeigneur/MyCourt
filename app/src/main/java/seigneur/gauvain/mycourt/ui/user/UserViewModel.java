package seigneur.gauvain.mycourt.ui.user;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;

public class UserViewModel extends ViewModel {

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    public UserRepository mUserRepository;

    private CompositeDisposable mCompositeDisposable    = new CompositeDisposable();
    private MutableLiveData<User> userMutableLiveData   = new MutableLiveData<>();
    private SingleLiveEvent<Void> mShowSourceCommand    = new SingleLiveEvent<>();

    private boolean isFetchFromAPI = false;
    private boolean isUserDirty = false;

    @Inject
    public UserViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        mCompositeDisposable.clear();
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN Fragment
     *********************************************************************************************/
    public void init() {
        //Current user is at least fetch from API, so don't request it again unless a refresh
        // is called from user (isUserDirty)
        Timber.d("isfetchFromapi?"+ isFetchFromAPI);
        if (isFetchFromAPI && getUser().getValue()!=null) {
            Timber.d("do not fetch user again");
            return;
        }
        Timber.d("fetch user ");
        fetchUser();

    }


    /**
     *
     * @return
     */
    public LiveData<User> getUser() {
        return userMutableLiveData;
    }

    /**
     * get user from both source and display info
     */
    private void fetchUser() {
        mCompositeDisposable.add(
                mUserRepository.getUser(false)
                        .subscribe(
                                this::onUserFound,              //User found - display info
                                this::onErrorHappened,          //Error happened during the request
                                this::manageUIFromDataSource    //Manage UI according to data source
                        )
        );
    }

    /**
     * Display user information according to:
     *  data source,
     *  available data,
     *  user profile
     * @param user object
     */
    private void onUserFound(User user) {
        Timber.d("user found "+user.getName());
        userMutableLiveData.setValue(user);
    }

    /**
     * An error happened during the request, warn the user
     * @param t - Throwable
     */
    private void onErrorHappened(Throwable t) {
        Timber.d(t);
    }

    /**
     * display a message to user according to fetch result
     * and connectivity
     */
    private void manageUIFromDataSource() {
        if (!mUserRepository.isFetchFromDBSuccess() && !mUserRepository.isFetchFromAPISuccess()) {
            isFetchFromAPI=false;
            //mUserView.showNoUserFoundView(true); //todo - replace
        } else if (!mUserRepository.isFetchFromDBSuccess()) {
            Timber.d("user fetch from api only");
            isFetchFromAPI=true;
        } else if (!mUserRepository.isFetchFromAPISuccess()){
            isFetchFromAPI=false;
            Timber.d("user fetch from DB only");
        } else {
            isFetchFromAPI=true;
            Timber.d("user from both source");
        }
    }

}
