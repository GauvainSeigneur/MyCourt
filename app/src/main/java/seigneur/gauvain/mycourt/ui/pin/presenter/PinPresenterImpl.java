package seigneur.gauvain.mycourt.ui.pin.presenter;

import android.service.quicksettings.Tile;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.model.Pin;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.pin.view.PinView;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;


@PerActivity
public class PinPresenterImpl implements PinPresenter {

    @Inject
    PinView mPinView;

    @Inject
    UserRepository mUserRepository;

    @Inject
    EnCryptor mEnCryptor;

    @Inject
    DeCryptor mDeCryptor;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    byte[] iV;

    boolean isDeCryptorEnabled=false;

    Pin mTempPin=null;

    @Inject
    public PinPresenterImpl() {
    }


    @Override
    public void onAttach() {
        checkIfPinAlreadyExists();

        //cryptAndSavepPin("password");
        //initDeCryptor();
    }

    @Override
    public void onDetach() {
        mCompositeDisposable.dispose();
        mPinView=null;
    }


    @Override
    public void  onPinConfirmed(String pin) {
    }

    @Override
    public void  onCheckPinSuccess() {}

    @Override
    public void  onCheckPinFailed() {}

    @Override
    public void  onPinReady() {}

    /*
     *******************************************************************************
     * Check if a pin code has been registered
     * if true, ask user the current one in order to change it
     * if false, set up UI to define pin code
     *******************************************************************************/
    public void checkIfPinAlreadyExists() {
        mCompositeDisposable.add(mUserRepository.getPin()
                .subscribe(
                        this::onPinAlreadyExists,
                        this::onCheckPinFailed,
                        this::onNoPinFound
                )
        );
    }

    private void onPinAlreadyExists(Pin pin) {
        mTempPin = pin;
        //todo - make view call to show a dialog to user ask him to confirm current pin
    }

    private void onCheckPinFailed(Throwable t) {

    }

    private void onNoPinFound() {

    }

    /*
     *******************************************************************************
     * Crypt and save pin
     *******************************************************************************/
    public void cryptAndStorePin(String pin) {
        mCompositeDisposable.add(Single.fromCallable(() -> {
            return  mEnCryptor.initCiper(Constants.SECRET_PWD_ALIAS);
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        cipher -> {
                            onCipherInit(cipher, pin);
                        },
                        t -> Timber.d(t)
                )
        );
    }

    private void onCipherInit(Cipher cipher, String pin) {
        iV = cipher.getIV();
        cryptPin(cipher, pin);
    }

    private void cryptPin(Cipher cipher, String pin) {
        mCompositeDisposable.add(Single.fromCallable(() -> {
            return  mEnCryptor.encryptedPin(cipher, pin);
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::onEncrypted,
                        t -> Timber.d(t)
                )
        );
    }

    private void onEncrypted(byte[] crypted) {
        Timber.d("Success crypto: " + Base64.encodeToString(crypted, Base64.DEFAULT));
        storePIN(createPin(0,crypted, iV));
    }

    /**
     * saved encrypted pin code in User DB
     * @param pin - string
     */
    private void storePIN(Pin pin) {
        mCompositeDisposable.add(mUserRepository.insertPin(pin)
                .subscribe(
                        () ->Timber.tag("kaaris").d("user pwd updated"),
                        t -> Timber.tag("kaaris").d(t) //Manage UI according to data source
                )
        );
    }

    /**
     * Create a PIN object
     * @param primaryKey
     * @param cryptedPin
     * @param initVector
     * @return pin
     */
    private Pin createPin(int primaryKey, byte[]  cryptedPin, byte[]  initVector) {
        return new Pin(
                primaryKey,
                cryptedPin,
                initVector
        );
    }

    /*
     *******************************************************************************
     * init DecCryptor and decrypt pin code
     *******************************************************************************/
    /**
     * Initialize Decryptor in order to call decryptText()
     */
    private void initDeCryptor() {
        mCompositeDisposable.add(Completable.fromAction(() -> {
            mDeCryptor.initKeyStore();
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::onDeCryptorInitialized,
                        this::onDeCryptorInitFailed
                )
        );
    }

    private void onDeCryptorInitialized() {
        isDeCryptorEnabled=true;
    }

    private void onDeCryptorInitFailed(Throwable t) {
        isDeCryptorEnabled=false;
    }

    private void deCryptPin(Pin pin) {
        if (isDeCryptorEnabled) {
            mCompositeDisposable.add(Single.fromCallable(() -> {
                return  mDeCryptor.decryptData(Constants.SECRET_PWD_ALIAS, pin.getCryptedPIN(), pin.getInitVector());
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            s -> Timber.tag("kaaris").d(s),
                            t -> Timber.tag("kaaris").d(t)
                    )
            );
        } else {
            //todo - make a view call
        }
    }


}
