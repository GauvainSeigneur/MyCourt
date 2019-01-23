package seigneur.gauvain.mycourt.ui.splash

import android.app.Activity
import android.app.Application
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import dagger.android.AndroidInjection
import seigneur.gauvain.mycourt.data.api.AuthUtils
import seigneur.gauvain.mycourt.ui.AuthActivity
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.main.MainActivity
import seigneur.gauvain.mycourt.ui.widget.ParallaxView

class SplashActivity : BaseActivity() {

    @Inject
    lateinit var mApplication: Application

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.layout_login)
    lateinit var mLoginLayout: LinearLayout

    @BindView(R.id.activity_login_btn)
    lateinit var loginBtn: Button

    @BindView(R.id.ball)
    lateinit var mball: ParallaxView

    @BindView(R.id.court)
    lateinit var mCourt: ParallaxView

    private val mSplashViewModel : SplashViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SplashViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_login)
        //init SplashViewModel
        mSplashViewModel.init()
        //Single are used to manage user click and single events messages
        subscribeToEvents(mSplashViewModel)
        ButterKnife.bind(this)
        mCourt.init()
        mCourt.setMinimumMovedPixelsToUpdate(mCourt.DEFAULT_MIN_MOVED_PIXELS * 3)
        mCourt.setMovementMultiplier(mCourt.DEFAULT_MOVEMENT_MULTIPLIER * 2f)

        mball.init()
        mball.setMinimumMovedPixelsToUpdate(mball.DEFAULT_MIN_MOVED_PIXELS * 5)
        mball.setMovementMultiplier(mball.DEFAULT_MOVEMENT_MULTIPLIER * 4f)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthUtils.REQ_CODE && resultCode == Activity.RESULT_OK) {
            val authCode = data!!.getStringExtra(AuthActivity.KEY_CODE)
            // splashPresenter.onSignInSuccess(authCode);  //todo -replace
            mSplashViewModel.onSignInSuccess(authCode)
        }
    }

    override fun onPause() {
        super.onPause()
        //stop listening accelerometer
        mball.unregisterSensorListener()
        mCourt.unregisterSensorListener()
    }

    override fun onResume() {
        super.onResume()
        //start listening accelerometer
        mball.registerSensorListener()
        mCourt.registerSensorListener()
    }


    @Optional
    @OnClick(R.id.activity_login_btn)
    fun signIn() {
        mSplashViewModel.onSignInClicked()
    }

    private fun subscribeToEvents(viewModel: SplashViewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.signInCommand.observe(this, Observer {
            goToAuthActivity()
        })

        viewModel.goToHomeCommand.observe(this, Observer { goToHome() })

        viewModel.isConnected.observe(this, Observer {
            if (it==false)
                mLoginLayout.visibility  = View.VISIBLE
            else
                mLoginLayout.visibility  = View.GONE
        })

    }

    private fun goToAuthActivity() {
        AuthUtils.openAuthActivity(this@SplashActivity)
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}