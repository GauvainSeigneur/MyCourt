package seigneur.gauvain.mycourt.data.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import timber.log.Timber;

public class UserViewModel extends ViewModel {

    @Inject
    UserRepository userRepo;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;
    //for using mutable live data : https://medium.com/@cdmunoz/offline-first-android-app-with-mvvm-dagger2-rxjava-livedata-and-room-part-4-2b476142e769

    //Mutable livedata to post and set value
    private MutableLiveData<User> userMutableLiveDatas = new MutableLiveData<>();
    private MutableLiveData<Throwable> errorMutableLiveData  = new MutableLiveData<>(); //must doesn't use it.. and use abstract MutableLiveData
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public UserViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        disposables.clear();
    }

    /*
     *********************************************************************************************
     * PUBLIC METHODS CALLED IN PRESENTER
     *********************************************************************************************/
    public void init() {
        //use livedata
        if (this.getUserMutableLiveDatas().getValue() != null) {
            Timber.d("user not null, do not perform another request...");
            return;
        }
        getUser(mConnectivityReceiver.isOnline());
    }

    /**
     * get the user fetch
     * @return
     */
    public LiveData<User> getUserMutableLiveDatas() {
        return userMutableLiveDatas;
    }

    /**
     * Error management with liveData
     * @return
     */
    public LiveData<Throwable> getErrorMutableLiveData() {
        return errorMutableLiveData;
    }
    /*
     *********************************************************************************************
     * REPOSITORY RELATIVE OPERATIONS
     *********************************************************************************************/
    //get RX value - must use state or a data wrapper : https://stackoverflow.com/questions/44208618/how-to-handle-error-states-with-livedata
    private void getUser(boolean applyResponseCache) {
        disposables.add(
                userRepo.getUser(applyResponseCache)
                        .subscribe(
                                user -> {
                                    userMutableLiveDatas.setValue(user); //set value for presenter
                                    Timber.d("user found");
                                },
                                t -> {
                                    errorMutableLiveData.setValue(t);
                                    Timber.d("error found");
                                },
                                () -> {
                                    Timber.d("complete");
                                }
                        )
        );
    }

}
