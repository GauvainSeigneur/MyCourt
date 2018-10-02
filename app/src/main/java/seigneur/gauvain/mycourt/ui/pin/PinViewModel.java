package seigneur.gauvain.mycourt.ui.pin;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
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
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;

public class PinViewModel extends ViewModel {

    @Inject
    UserRepository mUserRepository;

    @Inject
    EnCryptor mEnCryptor;

    @Inject
    DeCryptor mDeCryptor;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private MutableLiveData<Pin>     mStoredPin = new MutableLiveData<>(); //related to UI
    private MutableLiveData<Integer> mPinstep = new MutableLiveData<>(); //related to UI
    private SingleLiveEvent<String> mConfirmCurrentPin = new SingleLiveEvent<>(); //related to UI

    private byte[] iV; //do not set it as LiveData, we don't want to share it in the UI
    private boolean isDeCryptorEnabled=false; //do not set it as LiveData, we don't want to share it in the UI

    //TODO - must compare string between step 0 an step 1 before register pin

    @Inject
    public PinViewModel() {}

    public void init() {
        //Do not perform checking request if stored pin is already defined
        if (mStoredPin.getValue()!=null)
            return;
        checkIfPinAlreadyExists();
    }

    public LiveData<Integer> getStep() {
        return mPinstep;
    }

    @Override
    public void onCleared(){
        super.onCleared();
        Timber.d("viewmodel cleared");
        mCompositeDisposable.clear();
    }

    /**
     * If a stored pin exists, ask user to confirm it in order
     * to allow him to redefine it
     */
    public void onCurrentPinConfirmed(String pinInput) {
        Timber.d("onCurrentPinConfirmed");
        comparePinAndInput(pinInput);
    }

    /**
     *
     */
    public void onFirstPinDefined(String pin) {
        mPinstep.setValue(Constants.PIN_STEP_NEW_PIN_TWO); //set current step to 1
        //todo - live data single event
        /*if (mPinView!=null) {
            mPinView.showCreationPinStep(1);
        }*/
    }

    public void onNewPinConfirmed(String pin) {
        cryptAndStorePin(pin);
    }


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
        if (!pin.getCryptedPIN().isEmpty()) {
            mStoredPin.setValue(pin);
            mPinstep.setValue(Constants.PIN_STEP_CHECK_STORED); //set current step to -1
        } else {
            mPinstep.setValue(Constants.PIN_STEP_NEW_PIN_ONE); //set current step to -1
        }
    }

    private void onCheckPinExistsFailed(Throwable t) {
        Timber.d(t);
        //todo - what todo ? perform again and if the errors persists, delete the table ?
    }

    private void onNoPinFound() {
        mPinstep.setValue(Constants.PIN_STEP_NEW_PIN_ONE); //set current step to 0
        //todo - replace with liveData and single Event
        /*if (mPinView!=null)
            mPinView.showCreationPinStep(0);*/
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
        Timber.d("comparePinAndInput");
        if (mStoredPin.getValue()!=null) {
            initDeCryptorAndDecrypt(mStoredPin.getValue(), pinInput);
        } else {
            //get the current again and perform compare
        }
    }


    /**
     * Initialize Decryptor in order to call decryptText()
     */
    private void initDeCryptorAndDecrypt(Pin pin, String pinInput) {

        Timber.d("initDeCryptorAndDecrypt");
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

        Timber.d("onDeCryptorInitialized");
        deCryptPinAndCheckinput(pin, pinInput);
    }

    private void onDeCryptorInitFailed(Throwable t) {
        isDeCryptorEnabled=false;
    }

    private void deCryptPinAndCheckinput(Pin pin, String pinInput) {
        Timber.d("deCryptPinAndCheckinput");
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
            //todo - make an event here
        }
    }

    private void onCurrentPinDecrypted(String pinStored, String pinInput) {
        if (pinStored.equals(pinInput)) {
            Timber.d("good pin: " +pinStored);
        } else {
            Timber.d("wrong pin: " +pinStored);
        }
    }


}
