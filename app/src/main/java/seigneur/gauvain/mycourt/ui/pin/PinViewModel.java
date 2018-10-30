package seigneur.gauvain.mycourt.ui.pin;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import seigneur.gauvain.mycourt.data.model.Pin;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.ui.pin.tasks.CheckCurrentPinTask;
import seigneur.gauvain.mycourt.ui.pin.tasks.StorePinTask;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.SingleLiveEvent;
import seigneur.gauvain.mycourt.utils.crypto.DeCryptor;
import seigneur.gauvain.mycourt.utils.crypto.EnCryptor;
import timber.log.Timber;

public class PinViewModel extends ViewModel implements
        CheckCurrentPinTask.CheckCurrentPinCallBack, StorePinTask.StorePinCallback {

    @Inject
    public UserRepository mUserRepository;

    @Inject
    EnCryptor mEnCryptor;

    @Inject
    DeCryptor mDeCryptor;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private Pin mStoredPin = null; //not related to UI
    private MutableLiveData<Integer> mPinstep = new MutableLiveData<>(); //related to UI
    private SingleLiveEvent<String> mConfirmCurrentPin = new SingleLiveEvent<>(); //related to UI

    private byte[] iV; //do not set it as LiveData, we don't want to share it in the UI
    private boolean isDeCryptorEnabled=false; //do not set it as LiveData, we don't want to share it in the UI

    //TODO - must compare string between step 0 an step 1 before register pin

    private CheckCurrentPinTask mCheckCurrentPinTask;
    private StorePinTask mStorePinTask;

    @Inject
    public PinViewModel() {}

    public void init() {
        initTasks();
        //Do not perform checking request if stored pin is already defined
        if (mStoredPin!=null)
            return;
        mCheckCurrentPinTask.checkIfPinAlreadyExists();
    }

    private void initTasks() {
        if (mCheckCurrentPinTask==null)
            mCheckCurrentPinTask = new CheckCurrentPinTask(mCompositeDisposable,
                    mUserRepository, mDeCryptor, this);

        if (mStorePinTask==null)
            mStorePinTask = new StorePinTask(mCompositeDisposable,
                    mUserRepository, mEnCryptor, this);
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
        //comparePinAndInput(pinInput);
        mCheckCurrentPinTask.comparePinAndInput(mStoredPin, pinInput);
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
        mStorePinTask.cryptAndStorePin(pin);
    }

    /*
    *******************************************************************************
    * CheckCurrentPinTask.CheckCurrentPinCallBack
    *******************************************************************************/
    @Override
    public void onPinAlreadyStored(Pin pin) {
        mStoredPin = pin;
        mPinstep.setValue(Constants.PIN_STEP_CHECK_STORED); //set current step to -1
    }

    @Override
    public void onNoPinStored() {
        mPinstep.setValue(Constants.PIN_STEP_NEW_PIN_ONE);
    }

    @Override
    public void onPinCheckSuccess() {

    }

    @Override
    public void onCheckPinFailed() {

    }

    /*
    *******************************************************************************
    * StorePinTask.StorePinCallback
    *******************************************************************************/
    @Override
    public void onPinStored() {

    }

    @Override
    public void onStorePinFailed() {

    }

}
