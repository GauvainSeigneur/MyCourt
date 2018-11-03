package seigneur.gauvain.mycourt.ui.pin.tasks

import android.util.Base64

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import seigneur.gauvain.mycourt.data.model.Pin
import seigneur.gauvain.mycourt.data.repository.UserRepository
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor
import timber.log.Timber

class CheckCurrentPinTask(private val mCompositeDisposable: CompositeDisposable,
                          private val mUserRepository: UserRepository,
                          private val mDeCryptor: DeCryptor,
                          private val mCheckCurrentPinCallBack: CheckCurrentPinCallBack) {

    private var isDeCryptorEnabled = false

    /*
    *******************************************************************************
    * Check if a pin code has been registered
    * if true, ask user the current one in order to change it
    * if false, set up UI to define pin code
    *******************************************************************************/
    fun checkIfPinAlreadyExists() {
        mCompositeDisposable.add(mUserRepository.pin
                .subscribe(
                        Consumer<Pin> { this.onPinAlreadyExists(it) },
                        Consumer<Throwable> { this.onCheckPinExistsFailed(it) },
                        Action { this.onNoPinFound() }
                )
        )
    }

    private fun onPinAlreadyExists(pin: Pin) {
        if (!pin.cryptedPIN.isEmpty()) {
            mCheckCurrentPinCallBack.onPinAlreadyStored(pin)
        } else {
            mCheckCurrentPinCallBack.onNoPinStored()
            //mPinstep.setValue(Constants.PIN_STEP_NEW_PIN_ONE); //set current step to -1
        }
    }

    private fun onCheckPinExistsFailed(t: Throwable) {
        //todo - what todo ? perform again and if the errors persists, delete the table ?
        Timber.d(t)
    }

    private fun onNoPinFound() {
        mCheckCurrentPinCallBack.onNoPinStored()
        //mPinstep.setValue(Constants.PIN_STEP_NEW_PIN_ONE); //set current step to 0
    }

    /*
    *******************************************************************************
    * init DecCryptor and decrypt pin code to perform checking
    *******************************************************************************/
    fun comparePinAndInput(storedPin: Pin, pinInput: String) {
        Timber.d("comparePinAndInput")
        if (!isDeCryptorEnabled) {
            initDeCryptorAndDecrypt(storedPin, pinInput)
        } else {
            //get the current again and perform compare
            deCryptPinAndCheckInput(storedPin, pinInput)
        }
    }


    /**
     * Initialize Decryptor in order to call decryptText()
     */
    private fun initDeCryptorAndDecrypt(pin: Pin, pinInput: String) {
        Timber.d("initDeCryptorAndDecrypt")
        mCompositeDisposable.add(Completable.fromAction { mDeCryptor.initKeyStore() }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        Action { onDeCryptorInitialized(pin, pinInput) },
                        Consumer<Throwable> { this.onDeCryptorInitFailed(it) }
                )
        )
    }

    private fun onDeCryptorInitialized(pin: Pin, pinInput: String) {
        isDeCryptorEnabled = true
        Timber.d("onDeCryptorInitialized")
        deCryptPinAndCheckInput(pin, pinInput)
    }

    private fun onDeCryptorInitFailed(t: Throwable) {
        isDeCryptorEnabled = false
    }

    private fun deCryptPinAndCheckInput(pin: Pin, pinInput: String) {
        Timber.d("deCryptPinAndCheckinput")
        Timber.tag("cryptoTest").d(pin.cryptedPIN)
        if (isDeCryptorEnabled) {
            mCompositeDisposable.add(Single.fromCallable {
                mDeCryptor.decryptData(Constants.SECRET_PWD_ALIAS,
                        Base64.decode(pin.cryptedPIN, Base64.DEFAULT),
                        Base64.decode(pin.initVector, Base64.DEFAULT)
                )
            }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { s -> onCurrentPinDecrypted(s, pinInput) },
                            { t -> Timber.tag("cryptoTest").d(t) }
                    )
            )
        } else {
            //todo - make an event here
        }
    }

    private fun onCurrentPinDecrypted(pinStored: String, pinInput: String) {
        if (pinStored == pinInput) {
            mCheckCurrentPinCallBack.onPinCheckSuccess()
            Timber.d("good pin: $pinStored")
        } else {
            mCheckCurrentPinCallBack.onCheckPinFailed()
            Timber.d("wrong pin: $pinStored")
        }
    }

    interface CheckCurrentPinCallBack {

        fun onPinAlreadyStored(pin: Pin)

        fun onNoPinStored()

        fun onPinCheckSuccess()

        fun onCheckPinFailed()

    }

}
