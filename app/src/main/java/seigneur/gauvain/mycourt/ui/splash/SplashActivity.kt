package seigneur.gauvain.mycourt.ui.splash

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast


import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import dagger.android.AndroidInjection
import seigneur.gauvain.mycourt.data.api.AuthUtils
import seigneur.gauvain.mycourt.ui.AuthActivity
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.main.MainActivity
import timber.log.Timber

class SplashActivity : BaseActivity() {

    @Inject
    lateinit var mApplication: Application

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.activity_login_btn)
    lateinit var loginBtn: Button

    @BindView(R.id.welcomeTextView)
    lateinit var mWelcomeTextView: TextView

    private val mSplashViewModel : SplashViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SplashViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        AndroidInjection.inject(this)
        ButterKnife.bind(this)
        //init SplashViewModel
        mSplashViewModel.init()
        //Single are used to manage user click and single events messages
        suscribetoSingleEvents(mSplashViewModel)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthUtils.REQ_CODE && resultCode == Activity.RESULT_OK) {
            val authCode = data!!.getStringExtra(AuthActivity.KEY_CODE)
            // splashPresenter.onSignInSuccess(authCode);  //todo -replace
            mSplashViewModel.onSignInSuccess(authCode)

        }
    }

    @OnClick(R.id.activity_login_btn)
    fun signIn() {
        Timber.d("signin clicked")
        mSplashViewModel.onSignInClicked()
    }

    private fun suscribetoSingleEvents(viewModel: SplashViewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.signInCommand.observe(this, Observer {
            goToAuthActivity()
            Toast.makeText(mApplication, "toast", Toast.LENGTH_SHORT).show()
        })

        viewModel.goToHomeCommand.observe(this, Observer { goToHome() })

    }

    fun goToAuthActivity() {
        AuthUtils.openAuthActivity(this@SplashActivity)
    }

    fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}