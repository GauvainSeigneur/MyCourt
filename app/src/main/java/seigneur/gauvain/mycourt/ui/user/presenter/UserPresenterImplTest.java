package seigneur.gauvain.mycourt.ui.user.presenter;


import javax.inject.Inject;

import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.base.BasePresenterImplTest;
import seigneur.gauvain.mycourt.ui.user.view.UserViewTest;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;

@PerFragment
public class UserPresenterImplTest<V extends UserViewTest> extends BasePresenterImplTest<V> implements
        UserPresenterTest<V> {

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    UserRepository mUserRepository;

    @Inject
    EnCryptor mEnCryptor;

    @Inject
    DeCryptor mDeCryptor;

    //data managed by the presenter
    User mUser;

    @Inject
    public UserPresenterImplTest() {}

    @Override
    //@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onViewReady() {
        Timber.d("onViewReady");

        if (mUser==null)
            getUserAndDisplayIt(mConnectivityReceiver.isOnline());
        else
            onUserFound(mUser);
    }

    /**
     * get user from both source and display info
     * @param applyResponseCache - managed by internet connection state
     */
    private void getUserAndDisplayIt(boolean applyResponseCache) {
        getCompositeDisposable().add(mUserRepository.getUser(applyResponseCache)
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
            mUser=user;
            getMvpView().setUpUserAccountInfo(user);
            getMvpView().setUserPicture(user);
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
            getMvpView().showNoUserFoundView(true);
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
            getMvpView().showUserLinks(user);
            getMvpView().showNoLinksView(false);
        } else {
            getMvpView().showNoLinksView(true);
        }
    }

}
