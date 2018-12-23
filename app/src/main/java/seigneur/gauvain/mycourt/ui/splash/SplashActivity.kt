package seigneur.gauvain.mycourt.ui.splash

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast


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
import timber.log.Timber

class SplashActivity : BaseActivity() {

    @Inject
    lateinit var mApplication: Application

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.layout_login)
    lateinit var mLoginLayout: LinearLayout

    @BindView(R.id.activity_login_btn)
    lateinit var loginBtn: Button

    @BindView(R.id.welcomeTextView)
    lateinit var mWelcomeTextView: TextView

    private val mSplashViewModel : SplashViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SplashViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        //init SplashViewModel
        mSplashViewModel.init()
        setContentView(R.layout.activity_login)
        //Single are used to manage user click and single events messages
        subscribeToEvents(mSplashViewModel)
        ButterKnife.bind(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthUtils.REQ_CODE && resultCode == Activity.RESULT_OK) {
            val authCode = data!!.getStringExtra(AuthActivity.KEY_CODE)
            // splashPresenter.onSignInSuccess(authCode);  //todo -replace
            mSplashViewModel.onSignInSuccess(authCode)
        }
    }

    @Optional
    @OnClick(R.id.activity_login_btn)
    fun signIn() {
        Timber.d("signin clicked")
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