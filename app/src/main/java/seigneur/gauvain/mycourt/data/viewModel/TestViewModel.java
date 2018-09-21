package seigneur.gauvain.mycourt.data.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.data.repository.UserRepository;

public class TestViewModel extends ViewModel {

    //Data retauined inside ViewModel
    private LiveData<User> user;

    @Inject
    UserRepository userRepo;

    //only if use RX
    private Single<User> userSingle;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public int testInt = 0;

    @Inject
    public TestViewModel() {}

    @Override
    public void onCleared(){
        super.onCleared();
        disposables.clear();
    }

    // ----
    public void init() {
        //use livedata
        if (this.user != null) {
            return;
        }
        user = userRepo.getUserLive();

        //use rx
        /*if (this.userSingle != null) {
            return;
        }
        userSingle =  userRepo.getUserSingle(userId);*/

    }

    public LiveData<User> getUser() {
        return this.user;
    }

    //use RX
    public Single<User> getUserSingle() {
        return this.userSingle;
    }
    public CompositeDisposable getDisposables() {
        return disposables;
    }
}
