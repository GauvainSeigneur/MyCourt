package seigneur.gauvain.mycourt.ui.pin.presenter;


import android.util.Base64;
import javax.crypto.Cipher;
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

    private byte[] iV;
    private Pin mTempPin=null;
    private boolean isDeCryptorEnabled=false;

    @Inject
    public PinPresenterImpl() {
    }

    @Override
    public void onAttach() {
        checkIfPinAlreadyExists();
    }

    @Override
    public void onDetach() {
        mCompositeDisposable.dispose();
        mPinView=null;
    }

    @Override
    public void onFirstPinDefined(String pin) {
        if (mPinView!=null) {
            mPinView.showCreationPinStep(1);
        }
    }

    @Override
    public void  onNewPinConfirmed(String pin) {
        cryptAndStorePin(pin);
    }

    @Override
    public void onCurrentPinConfirmed(String pinInput) {
        comparePinAndInput(pinInput);
    }

    @Override
    public void  onCheckPinSuccess() {
        if (mPinView!=null)
            mPinView.showCreationPinStep(0);
    }

    @Override
    public void  onCheckPinFailed() {}

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
                        this::onCheckPinExistsFailed,
                        this::onNoPinFound
                )
        );
    }

    private void onPinAlreadyExists(Pin pin) {
        if (mPinView!=null) {
            if (pin.getCryptedPIN()!=null && !pin.getCryptedPIN().toString().isEmpty()) {
                mTempPin = pin;
                mPinView.showConfirmCurrentPinView(true);
            } else {
                onNoPinFound();
            }
        }

    }

    private void onCheckPinExistsFailed(Throwable t) {
        Timber.d(t);
        //todo - what todo ? perform again and if the errors persists, delete the table ?
    }

    private void onNoPinFound() {
        if (mPinView!=null)
            mPinView.showCreationPinStep(0);
    }

    /*
     *******************************************************************************
     * Crypt and save pin defined by user in EdiText
     *******************************************************************************/
    public void cryptAndStorePin(String pin) {
        Timber.tag("cryptoTest").d("cryptAndStorePin called");
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
        Timber.tag("cryptoTest").d("onCipherInit succeed");
        iV = cipher.getIV();
        cryptPin(cipher, pin);
    }

    private void cryptPin(Cipher cipher, String pin) {
        Timber.tag("cryptoTest").d("cryptPin called");
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
        Timber.tag("cryptoTest").d("Success crypto: " + Base64.encodeToString(crypted, Base64.DEFAULT) + " iv: " + Base64.encodeToString(iV, Base64.DEFAULT));
        storePIN(createPin(0,
                Base64.encodeToString(crypted, Base64.DEFAULT),
                Base64.encodeToString(iV, Base64.DEFAULT)
                )
        );
    }

    /**
     * saved encrypted pin code in User DB
     * @param pin - string
     */
    private void storePIN(Pin pin) {
        mCompositeDisposable.add(mUserRepository.insertPin(pin)
                .subscribe(
                        () ->Timber.tag("cryptoTest").d("user pin stored"),
                        t -> Timber.tag("cryptoTest").d(t) //Manage UI according to data source
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
    private Pin createPin(int primaryKey, String  cryptedPin, String  initVector) {
        return new Pin(
                primaryKey,
                cryptedPin,
                initVector
        );
    }

    /*
     *******************************************************************************
     * init DecCryptor and decrypt pin code to perform checking
     *******************************************************************************/
    private void comparePinAndInput(String pinInput) {
        if (mTempPin!=null) {
            initDeCryptorAndDecrypt(mTempPin, pinInput);
        } else {
            //get the current again and perform compare
        }
    }


    /**
     * Initialize Decryptor in order to call decryptText()
     */
    private void initDeCryptorAndDecrypt(Pin pin, String pinInput) {
        mCompositeDisposable.add(Completable.fromAction(() -> {
            mDeCryptor.initKeyStore();
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> onDeCryptorInitialized(pin, pinInput),
                        this::onDeCryptorInitFailed
                )
        );
    }

    private void onDeCryptorInitialized(Pin pin, String pinInput) {
        isDeCryptorEnabled=true;
        deCryptPinAndCheckinput(pin, pinInput);
    }

    private void onDeCryptorInitFailed(Throwable t) {
        isDeCryptorEnabled=false;
    }

    private void deCryptPinAndCheckinput(Pin pin, String pinInput) {
        Timber.tag("cryptoTest").d(pin.getCryptedPIN().toString());

        if (isDeCryptorEnabled) {
            mCompositeDisposable.add(Single.fromCallable(() -> {
                return  mDeCryptor.decryptData(Constants.SECRET_PWD_ALIAS,
                       Base64.decode( pin.getCryptedPIN(),Base64.DEFAULT),
                        Base64.decode( pin.getInitVector(),Base64.DEFAULT)
                        );
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            s  -> onCurrentPinDecrypted(s, pinInput),
                            t -> Timber.tag("cryptoTest").d(t)
                    )
            );
        } else {
            //todo - make a view call
        }
    }

    private void onCurrentPinDecrypted(String pinStored, String pinInput) {
        if (pinStored.equals(pinInput)) {
          Timber.tag("cryptoTest").d("good pin: " +pinStored);
        } else {
            Timber.tag("cryptoTest").d("wrong pin: " +pinStored);
        }
    }




}
