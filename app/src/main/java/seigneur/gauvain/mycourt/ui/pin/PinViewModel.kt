package seigneur.gauvain.mycourt.ui.pin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import javax.inject.Inject

import io.reactivex.disposables.CompositeDisposable
import seigneur.gauvain.mycourt.data.model.Pin
import seigneur.gauvain.mycourt.data.repository.UserRepository
import seigneur.gauvain.mycourt.ui.pin.tasks.CheckCurrentPinTask
import seigneur.gauvain.mycourt.ui.pin.tasks.StorePinTask
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.SingleLiveEvent
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor
import timber.log.Timber

class PinViewModel @Inject
constructor() : ViewModel(), CheckCurrentPinTask.CheckCurrentPinCallBack, StorePinTask.StorePinCallback {

    @Inject
    lateinit var mUserRepository: UserRepository

    @Inject
    lateinit var mEnCryptor: EnCryptor

    @Inject
    lateinit var mDeCryptor: DeCryptor

    private val mCompositeDisposable = CompositeDisposable()

    private var mStoredPin: Pin? = null //not related to UI
    private val mPinstep = MutableLiveData<Int>() //related to UI
    private val mConfirmCurrentPin = SingleLiveEvent<String>() //related to UI

    private val iV: ByteArray? = null //do not set it as LiveData, we don't want to share it in the UI
    private val isDeCryptorEnabled = false //do not set it as LiveData, we don't want to share it in the UI

    //TODO - must compare string between step 0 an step 1 before register pin

    private val mCheckCurrentPinTask: CheckCurrentPinTask by lazy {
        CheckCurrentPinTask(mCompositeDisposable,
            mUserRepository, mDeCryptor, this) }

    private val mStorePinTask: StorePinTask by lazy {
        StorePinTask(mCompositeDisposable,
                mUserRepository, mEnCryptor, this)
    }

    val step: LiveData<Int>
        get() = mPinstep

    fun init() {
        //Do not perform checking request if stored pin is already defined
        if (mStoredPin != null)
            return
        mCheckCurrentPinTask.checkIfPinAlreadyExists()
    }


    public override fun onCleared() {
        super.onCleared()
        Timber.d("viewmodel cleared")
        mCompositeDisposable.clear()
    }

    /**
     * If a stored pin exists, ask user to confirm it in order
     * to allow him to redefine it
     */
    fun onCurrentPinConfirmed(pinInput: String) {
        Timber.d("onCurrentPinConfirmed")
        //comparePinAndInput(pinInput);
        mCheckCurrentPinTask.comparePinAndInput(mStoredPin!!, pinInput)
    }

    /**
     *
     */
    fun onFirstPinDefined(pin: String) {
        mPinstep.value = Constants.PIN_STEP_NEW_PIN_TWO //set current step to 1
        //todo - live data single event
        /*if (mPinView!=null) {
            mPinView.showCreationPinStep(1);
        }*/
    }

    fun onNewPinConfirmed(pin: String) {
        mStorePinTask.cryptAndStorePin(pin)
    }

    /*
    *******************************************************************************
    * CheckCurrentPinTask.CheckCurrentPinCallBack
    *******************************************************************************/
    override fun onPinAlreadyStored(pin: Pin) {
        mStoredPin = pin
        mPinstep.value = Constants.PIN_STEP_CHECK_STORED //set current step to -1
    }

    override fun onNoPinStored() {
        mPinstep.value = Constants.PIN_STEP_NEW_PIN_ONE
    }

    override fun onPinCheckSuccess() {

    }

    override fun onCheckPinFailed() {

    }

    /*
    *******************************************************************************
    * StorePinTask.StorePinCallback
    *******************************************************************************/
    override fun onPinStored() {

    }

    override fun onStorePinFailed() {

    }

}
