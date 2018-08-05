package seigneur.gauvain.mycourt.ui.user.presenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.data.api.DribbbleService;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.user.view.UserView;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import timber.log.Timber;

//todo :
// 1) Fetch user info from api
// 2) store it in database
// 3) display user info from database (even if api request has succeed or not)

//test : https://medium.com/mobiwise-blog/load-cache-before-api-call-observable-concat-f527f267656
@PerFragment
public class UserPresenterImpl implements UserPresenter {

    @Inject
    UserView mUserView;

    @Inject
    ConnectivityReceiver mConnectivityReceiver;

    @Inject
    UserRepository userRepository;

    private CompositeDisposable compositeDisposable;

    @Inject
    public UserPresenterImpl() {
    }


    @Override
    public void onAttach() {
        compositeDisposable = new CompositeDisposable();
        getUserAndDisplayIt(mConnectivityReceiver.isOnline());
    }

    @Override
    public void onDetach() {
        compositeDisposable.dispose();
        mUserView=null;
    }

    /**
     * Get user from DB and from API
     */
    private void getUserAndDisplayIt(boolean applyResponseCache) {
        compositeDisposable.add(userRepository.getUser(applyResponseCache)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        displayUserInfo(user);
                        Timber.d(user.getFollowers_count()+"");
                    }
                })
                .doOnComplete(this::manageUserUI)
                .subscribe()
        );
    }

    /**
     * Display user information according to:
     *  data source,
     *  available data,
     *  user profile
     * @param user object
     */
    private void displayUserInfo(User user) {
        if(mUserView!=null) {
            mUserView.setUpUserAccountInfo(user);
            mUserView.setUserPicture(user);
            showUserLink(user);
        }
    }

    private void showUserLink(User user) {
        if (!user.getLinks().isEmpty()) {
            mUserView.showUserLinks(user);
            mUserView.showNoLinksView(false);
        } else {
            mUserView.showNoLinksView(true);
        }
    }

    /**
     * display a message to user according to fetch result
     * and connectivity
     */
    private void manageUserUI() {
        Timber.d(String.valueOf(userRepository.isFetchFromAPISuccess)+" "+String.valueOf(userRepository.isFetchFromDBSuccess));
        if (!userRepository.isFetchFromDBSuccess && !userRepository.isFetchFromAPISuccess) {
            mUserView.showNoUserFoundView(true);
        } else if (!userRepository.isFetchFromDBSuccess) {
            Timber.d("user fetch from api only");
        } else if (!userRepository.isFetchFromAPISuccess){
            Timber.d("user fetch from DB only");
        } else {
            Timber.d("user from both source");
        }
    }


}
