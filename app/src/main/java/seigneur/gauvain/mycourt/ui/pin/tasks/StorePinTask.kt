package seigneur.gauvain.mycourt.ui.pin.tasks

import android.util.Base64

import javax.crypto.Cipher

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import seigneur.gauvain.mycourt.data.model.Pin
import seigneur.gauvain.mycourt.data.repository.UserRepository
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor
import timber.log.Timber

class StorePinTask(private val mCompositeDisposable: CompositeDisposable,
                   private val mUserRepository: UserRepository,
                   private val mEnCryptor: EnCryptor,
                   private val mStorePinCallback: StorePinCallback) {

    private var iV: ByteArray? =  null//do not set it as LiveData, we don't want to share it in the UI

    /*
     *******************************************************************************
     * Crypt and save pin defined by user in EdiText
     *******************************************************************************/
    fun cryptAndStorePin(pin: String) {
        Timber.tag("cryptoTest").d("cryptAndStorePin called")
        mCompositeDisposable.add(Single.fromCallable { mEnCryptor.initCiper(Constants.SECRET_PWD_ALIAS) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { cipher -> onCipherInit(cipher, pin) },
                        { t -> Timber.d(t) }
                )
        )
    }

    private fun onCipherInit(cipher: Cipher, pin: String) {
        Timber.tag("cryptoTest").d("onCipherInit succeed")
        iV = cipher.iv
        cryptPin(cipher, pin)
    }

    private fun cryptPin(cipher: Cipher, pin: String) {
        Timber.tag("cryptoTest").d("cryptPin called")
        mCompositeDisposable.add(Single.fromCallable { mEnCryptor.encryptedPin(cipher, pin) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        Consumer<ByteArray> { this.onEncrypted(it) },
                        Consumer<Throwable>  { Timber.d(it) }
                )
        )
    }

    private fun onEncrypted(crypted: ByteArray) {
        Timber.tag("cryptoTest").d("Success crypto: " + Base64.encodeToString(crypted, Base64.DEFAULT) + " iv: " + Base64.encodeToString(iV, Base64.DEFAULT))
        storePIN(createPin(0,
                Base64.encodeToString(crypted, Base64.DEFAULT),
                Base64.encodeToString(iV, Base64.DEFAULT)
        )
        )
    }

    /**
     * saved encrypted pin code in User DB
     * @param pin - string
     */
    private fun storePIN(pin: Pin) {
        mCompositeDisposable.add(mUserRepository.insertPin(pin)
                .subscribe(
                        {
                            mStorePinCallback.onPinStored()
                            Timber.tag("cryptoTest").d("user pin stored")
                        },
                        { t ->
                            mStorePinCallback.onStorePinFailed()
                            Timber.tag("cryptoTest").d(t) //Manage UI according to data source
                        }
                )
        )
    }

    /**
     * Create a PIN object
     * @param primaryKey
     * @param cryptedPin
     * @param initVector
     * @return pin
     */
    private fun createPin(primaryKey: Int, cryptedPin: String, initVector: String): Pin {
        return Pin(
                primaryKey,
                cryptedPin,
                initVector
        )
    }

    interface StorePinCallback {

        fun onPinStored()

        fun onStorePinFailed()

    }

}
