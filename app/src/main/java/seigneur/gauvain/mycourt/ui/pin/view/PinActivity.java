package seigneur.gauvain.mycourt.ui.pin.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

import com.alimuzaffar.lib.pin.PinEntryEditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.ui.pin.presenter.PinPresenter;

public class PinActivity extends BaseActivity implements PinView {

    @Inject
    PinPresenter<PinView> mPinPresenter;

    @BindView(R.id.pinEditor)
    PinEntryEditText mPinEditor;

    @BindView(R.id.confirmPinBtn)
    Button mConfirmPinBtn;

    @BindView(R.id.stubPinChecker)
    ViewStub mStubPinChecker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.activity_pin);
        ButterKnife.bind(this);
        mPinPresenter.onAttach(this);
        mPinPresenter.onViewReady();
        if (mPinEditor != null) {
            mPinEditor.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    mPinPresenter.onNewPinConfirmed(str.toString());
                }
            });
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPinPresenter.onDetach();
    }

    @Override
    public void showConfirmCurrentPinView(boolean isVisible) {
        if (isVisible) {
            View inflated = mStubPinChecker.inflate ();
            ConfirmPinViewStub confirmPinViewStub = new ConfirmPinViewStub(inflated);
            confirmPinViewStub.currentPinInput.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    mPinPresenter.onCurrentPinConfirmed(str.toString());
                }
            });
        }
        else {
            mStubPinChecker.setVisibility(View.GONE);
        }
    }


    @Override
    public void showCreationPinStep(int step) {
        switch (step) {
            case 0 :
            case 1 :
            default:
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
