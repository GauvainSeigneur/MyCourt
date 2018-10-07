package seigneur.gauvain.mycourt.ui.pin.tasks;

import android.util.Base64;

import javax.crypto.Cipher;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import seigneur.gauvain.mycourt.data.model.Pin;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;

public class StorePinTask {

    private CompositeDisposable mCompositeDisposable;

    private EnCryptor mEnCryptor;

    private UserRepository mUserRepository;

    private StorePinCallback mStorePinCallback;

    private byte[] iV; //do not set it as LiveData, we don't want to share it in the UI

    public StorePinTask(CompositeDisposable compositeDisposable,
                        UserRepository userRepository,
                        EnCryptor enCryptor,
                        StorePinCallback storePinCallback){
        this.mCompositeDisposable=compositeDisposable;
        this.mUserRepository=userRepository;
        this.mEnCryptor=enCryptor;
        this.mStorePinCallback=storePinCallback;
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
                        () -> {
                            mStorePinCallback.onPinStored();
                            Timber.tag("cryptoTest").d("user pin stored");
                        },
                        t -> {
                            mStorePinCallback.onStorePinFailed();
                            Timber.tag("cryptoTest").d(t); //Manage UI according to data source
                        }
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

    public interface StorePinCallback {

        void onPinStored();

        void onStorePinFailed();

    }

}
