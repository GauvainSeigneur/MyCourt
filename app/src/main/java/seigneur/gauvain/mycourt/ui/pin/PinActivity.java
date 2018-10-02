package seigneur.gauvain.mycourt.ui.pin;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.utils.Constants;

public class PinActivity extends BaseActivity {

   /* @Inject
    PinPresenter mPinPresenter;*/

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private PinViewModel mPinViewModel;

    @BindView(R.id.pinEditor)
    PinEntryEditText mPinEditor;

    @BindView(R.id.confirmPinBtn)
    Button mConfirmPinBtn;

    @BindView(R.id.stubPinChecker)
    ViewStub mStubPinChecker;
    //new instance of view stub to bind views with butterknife;
    ConfirmPinViewStub confirmPinViewStub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.activity_pin);
        ButterKnife.bind(this);

        mPinViewModel = ViewModelProviders.of(this, viewModelFactory).get(PinViewModel.class);
        mPinViewModel.init();
        subscribeToLiveData(mPinViewModel);

        if (mPinEditor != null) {
            mPinEditor.setOnPinEnteredListener(str -> {
                mPinViewModel.onNewPinConfirmed(str.toString());
            });
        }

    }

    private void subscribeToLiveData(PinViewModel pinViewModel) {
        //observe current step
        pinViewModel.getStep().observe(this, this::setStepUI);
    }

    private void setStepUI(int step) {
        switch (step) {
            case Constants.PIN_STEP_CHECK_STORED :
                showConfirmCurrentPinView(true);
                break;
            case Constants.PIN_STEP_NEW_PIN_ONE :
                showConfirmCurrentPinView(false);
                break;
            case Constants.PIN_STEP_NEW_PIN_TWO :
                showConfirmCurrentPinView(false);
                break;
            default:
                showConfirmCurrentPinView(false);
        }
    }

    /**
     * user proposes string fro current pin code, check it in database
     * @param isVisible - check vsisiblity
     */
    public void showConfirmCurrentPinView(boolean isVisible) {
        if (isVisible) {
            if (mStubPinChecker.getParent() != null) {
                View inflated = mStubPinChecker.inflate();
                confirmPinViewStub = new ConfirmPinViewStub(inflated);
            } else {
                mStubPinChecker.setVisibility(View.VISIBLE);
            }
            //listen Pin entry
            confirmPinViewStub.currentPinInput.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    mPinViewModel.onCurrentPinConfirmed(str.toString());
                }
            });

        }
        else {
            mStubPinChecker.setVisibility(View.GONE);
        }
    }

    /**
     * Inner class to bind views from ViewStub
     */
    public class ConfirmPinViewStub {
        @BindView(R.id.current_pin_checker)
        PinEntryEditText currentPinInput;
        public ConfirmPinViewStub(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
