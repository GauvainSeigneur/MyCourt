package seigneur.gauvain.mycourt.ui.pin.tasks;

import android.util.Base64;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.model.Pin;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import timber.log.Timber;

public class CheckCurrentPinTask {

    private CompositeDisposable mCompositeDisposable;

    private UserRepository mUserRepository;

    private DeCryptor mDeCryptor;

    private CheckCurrentPinCallBack mCheckCurrentPinCallBack;

    private boolean isDeCryptorEnabled=false;

    public CheckCurrentPinTask(CompositeDisposable compositeDisposable,
                               UserRepository userRepository,
                               DeCryptor deCryptor,
                               CheckCurrentPinCallBack checkCurrentPinCallBack) {
        this.mCompositeDisposable=compositeDisposable;
        this.mUserRepository=userRepository;
        this.mDeCryptor=deCryptor;
        this.mCheckCurrentPinCallBack=checkCurrentPinCallBack;
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
            mCheckCurrentPinCallBack.onPinAlreadyStored(pin);
        } else {
            mCheckCurrentPinCallBack.onNoPinStored();
            //mPinstep.setValue(Constants.PIN_STEP_NEW_PIN_ONE); //set current step to -1
        }
    }

    private void onCheckPinExistsFailed(Throwable t) {
        //todo - what todo ? perform again and if the errors persists, delete the table ?
        Timber.d(t);
    }

    private void onNoPinFound() {
        mCheckCurrentPinCallBack.onNoPinStored();
        //mPinstep.setValue(Constants.PIN_STEP_NEW_PIN_ONE); //set current step to 0
    }

    /*
    *******************************************************************************
    * init DecCryptor and decrypt pin code to perform checking
    *******************************************************************************/
    public void comparePinAndInput(Pin storedPin, String pinInput) {
        Timber.d("comparePinAndInput");
        if (!isDeCryptorEnabled) {
            initDeCryptorAndDecrypt(storedPin, pinInput);
        } else {
            //get the current again and perform compare
            deCryptPinAndCheckInput(storedPin, pinInput);
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
        deCryptPinAndCheckInput(pin, pinInput);
    }

    private void onDeCryptorInitFailed(Throwable t) {
        isDeCryptorEnabled=false;
    }

    private void deCryptPinAndCheckInput(Pin pin, String pinInput) {
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
            mCheckCurrentPinCallBack.onPinCheckSuccess();
            Timber.d("good pin: " +pinStored);
        } else {
            mCheckCurrentPinCallBack.onCheckPinFailed();
            Timber.d("wrong pin: " +pinStored);
        }
    }

    public interface CheckCurrentPinCallBack {

        void onPinAlreadyStored(Pin pin);

        void onNoPinStored();

        void onPinCheckSuccess();

        void onCheckPinFailed();

    }

}
