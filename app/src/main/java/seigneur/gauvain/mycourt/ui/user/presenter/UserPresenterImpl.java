package seigneur.gauvain.mycourt.ui.user.presenter;


import android.arch.lifecycle.LifecycleOwner;

import javax.inject.Inject;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.data.viewModel.UserViewModel;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.user.view.UserView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;

@PerFragment
public class UserPresenterImpl implements UserPresenter {

    UserView mUserView;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    UserRepository mUserRepository;

    @Inject
    EnCryptor mEnCryptor;

    @Inject
    DeCryptor mDeCryptor;

    private CompositeDisposable mCompositeDisposable;

    private UserViewModel mUserViewModel;

    private LifecycleOwner mLifecycleOwner;

    @Inject
    public UserPresenterImpl(
            UserView userView,
             UserViewModel userViewModel,
             LifecycleOwner lifecycleOwner) {
        this.mUserView=userView;
        this.mUserViewModel=userViewModel;
        this.mLifecycleOwner=lifecycleOwner;
    }


    @Override
    public void onAttach() {
        mUserViewModel.init(); //get info from repository
        mCompositeDisposable = new CompositeDisposable();
        //getUserAndDisplayIt(mConnectivityReceiver.isOnline());
    }

    @Override
    public void getUser() {
        getUserFromViewModel();
    }

    @Override
    public void onDetach() {
        mCompositeDisposable.dispose();
        mUserView=null;
    }

    private void getUserFromViewModel() {
        Timber.d("getUserFromViewModel");
        //good result
        mUserViewModel.getUserMutableLiveDatas() //
                .observe(
                        mLifecycleOwner,
                        this::onUserFound
                );
        //error result
        mUserViewModel.getErrorMutableLiveData() //
                .observe(
                        mLifecycleOwner,
                        this::onErrorHappened
                );
            }

    /**
     * get user from both source and display info
     * @param applyResponseCache - managed by internet connection state
     */
    private void getUserAndDisplayIt(boolean applyResponseCache) {
        mCompositeDisposable.add(
                mUserRepository.getUser(applyResponseCache)
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

            mUserView.setUpUserAccountInfo(user);
            mUserView.setUserPicture(user);
            showUserLink(user);

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
        if (!mUserRepository.isFetchFromDBSuccess && !mUserRepository.isFetchFromAPISuccess) {
            mUserView.showNoUserFoundView(true);
        } else if (!mUserRepository.isFetchFromDBSuccess) {
            Timber.d("user fetch from api only");
        } else if (!mUserRepository.isFetchFromAPISuccess){
            Timber.d("user fetch from DB only");
        } else {
            Timber.d("user from both source");
        }
    }

    /**
     * Show user Links
     * @param user - User fetched
     */
    private void showUserLink(User user) {
        if (!user.getLinks().isEmpty()) {
            mUserView.showUserLinks(user);
            mUserView.showNoLinksView(false);
        } else {
            mUserView.showNoLinksView(true);
        }
    }

}
