package seigneur.gauvain.mycourt.ui.user.presenter;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import javax.inject.Inject;
import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.user.view.UserView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;

@PerFragment
public class UserPresenterImpl implements UserPresenter, LifecycleObserver {

    @Inject
    UserView mUserView;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    UserRepository mUserRepository;

    @Inject
    EnCryptor mEnCryptor;

    @Inject
    DeCryptor mDeCryptor;

    private LifecycleObserver mLifecycleObserver;
    private CompositeDisposable mCompositeDisposable;

    //data managed by the presenter
    User mUser;

    @Inject
    public UserPresenterImpl() {}

    @Override
    public void onAttach() {
        if (mUserView instanceof LifecycleOwner && mLifecycleObserver==null) {
            ((LifecycleOwner) mUserView).getLifecycle().addObserver(this);
            Timber.d("addObserver");
            mLifecycleObserver=this;
        }
        if (mCompositeDisposable==null)
            mCompositeDisposable = new CompositeDisposable();

       if (mUser==null)
           getUserAndDisplayIt(mConnectivityReceiver.isOnline());
       else
           onUserFound(mUser);
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onViewReady() {
        Timber.d("onViewReady");
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDetach() {
        // Clean up any no-longer-use resources here
        mUserView=null;
        mCompositeDisposable.dispose();
        ((LifecycleOwner) mUserView).getLifecycle().removeObserver(this);
        mLifecycleObserver=null;
    }

    /**
     * get user from both source and display info
     * @param applyResponseCache - managed by internet connection state
     */
    private void getUserAndDisplayIt(boolean applyResponseCache) {
        mCompositeDisposable.add(mUserRepository.getUser(applyResponseCache)
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
        if(mUserView!=null) {
            mUser=user;
            mUserView.setUpUserAccountInfo(user);
            mUserView.setUserPicture(user);
            showUserLink(user);
        }
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
