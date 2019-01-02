package seigneur.gauvain.mycourt.ui.pin

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.Toast

import com.alimuzaffar.lib.pin.PinEntryEditText

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.utils.Constants

class PinActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mPinViewModel: PinViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PinViewModel::class.java)
    }

    @BindView(R.id.pinEditor)
    lateinit var mPinEditor: PinEntryEditText

    @BindView(R.id.confirmPinBtn)
    lateinit var mConfirmPinBtn: Button

    @BindView(R.id.stubPinChecker)
    lateinit var mStubPinChecker: ViewStub
    //new instance of view stub to bind views with butterknife;
    private lateinit var confirmPinViewStub: ConfirmPinViewStub


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_pin)
        ButterKnife.bind(this)
        mPinViewModel.init()
        subscribeToLiveData(mPinViewModel)
        mPinEditor.setOnPinEnteredListener { str -> mPinViewModel.onNewPinConfirmed(str.toString()) }


    }

    private fun subscribeToLiveData(pinViewModel: PinViewModel) {
        //observe current step
        pinViewModel.step.observe(this, Observer<Int> { this.setStepUI(it!!) })
    }


    private fun setStepUI(step: Int) {
        when (step) {
            Constants.PIN_STEP_CHECK_STORED -> showConfirmCurrentPinView(true)
            Constants.PIN_STEP_NEW_PIN_ONE -> showConfirmCurrentPinView(false)
            Constants.PIN_STEP_NEW_PIN_TWO -> showConfirmCurrentPinView(false)
            else -> showConfirmCurrentPinView(false)
        }
    }

    /**
     * user proposes string fro current pin code, check it in database
     * @param isVisible - check vsisiblity
     */
    private fun showConfirmCurrentPinView(isVisible: Boolean) {
        if (isVisible) {
            if (mStubPinChecker.parent != null) {
                val inflated = mStubPinChecker.inflate()
                confirmPinViewStub = ConfirmPinViewStub(inflated)
            } else {
                mStubPinChecker.visibility = View.VISIBLE
            }
            //listen Pin entry
            confirmPinViewStub.currentPinInput.setOnPinEnteredListener { str -> mPinViewModel.onCurrentPinConfirmed(str.toString()) }

        } else {
            mStubPinChecker.visibility = View.GONE
        }
    }

    /**
     * Inner class to bind views from ViewStub
     */
    inner class ConfirmPinViewStub(view: View) {
        @BindView(R.id.current_pin_checker)
        lateinit var currentPinInput: PinEntryEditText

        init {
            ButterKnife.bind(this, view)
        }
    }


}
