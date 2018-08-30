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
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor_Factory;
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
        //encryptPwd("myCustomPassword");
        //getUserAndDisplayItOnlyFromDB(false);

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

    /*
     *******************************************************************************
     * ONLY FOR TESTS
     *******************************************************************************/
    private void getUserAndDisplayItOnlyFromDB(boolean applyResponseCache) {
        mCompositeDisposable.add(mUserRepository.getUserFromDB()
                .subscribe(
                        this::onUserFoundTest,              //User found - display info
                        t -> Timber.tag("lololol").d(t) ,         //Error happened during the request
                        () ->Timber.tag("lololol").d("oncomplete")   //Manage UI according to data source
                )
        );
    }

    private void onUserFoundTest(User user) {
        if(mUserView!=null) {
            mUserView.setUpUserAccountInfo(user);
            mUserView.setUserPicture(user);
            showUserLink(user);
            if(user.getCryptedPwd()!=null)
                Timber.tag("lololol").d(user.getCryptedPwd());
            else {
                //updateUserPWD("passwordTest");
            }
        }
    }

    private void updateUserPWD(String pwd) {
        mCompositeDisposable.add(mUserRepository.updateUserPWD(pwd)
                .subscribe(
                        () ->Timber.tag("lololol").d("user pwd updated"),
                        t -> Timber.tag("lololol").d(t) //Manage UI according to data source
                )
        );
    }

    private void encryptPwd(String pwd) {
        try {
            final byte[] encryptedText = mEnCryptor.encryptText(Constants.SECRET_PWD_ALIAS, pwd);
            updateUserPWD(Base64.encodeToString(encryptedText, Base64.DEFAULT));
            //tvEncryptedText.setText(Base64.encodeToString(encryptedText, Base64.DEFAULT));
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | NoSuchProviderException |
                KeyStoreException | IOException | NoSuchPaddingException | InvalidKeyException e) {
            Log.e(TAG, "onClick() called with: " + e.getMessage(), e);
        } catch (InvalidAlgorithmParameterException | SignatureException |
                IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }


}
