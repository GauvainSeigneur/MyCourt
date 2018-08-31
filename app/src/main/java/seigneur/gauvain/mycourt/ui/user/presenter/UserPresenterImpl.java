package seigneur.gauvain.mycourt.ui.user.presenter;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;

import static com.yalantis.ucrop.UCropFragment.TAG;

@PerFragment
public class UserPresenterImpl implements UserPresenter {

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

    private CompositeDisposable mCompositeDisposable;

    @Inject
    public UserPresenterImpl() {
    }


    @Override
    public void onAttach() {
        mCompositeDisposable = new CompositeDisposable();
        getUserAndDisplayIt(mConnectivityReceiver.isOnline());
    }

    @Override
    public void onDetach() {
        mCompositeDisposable.dispose();
        mUserView=null;
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
