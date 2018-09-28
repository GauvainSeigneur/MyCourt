package seigneur.gauvain.mycourt.ui.splash;

import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import seigneur.gauvain.mycourt.data.api.AuthUtils;
import seigneur.gauvain.mycourt.ui.AuthActivity;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.ui.main.MainActivity;

public class SplashActivity extends BaseActivity {

    @Inject
    Application mApplication;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private SplashViewModel mSplashViewModel;

    @BindView(R.id.activity_login_btn)
    Button loginBtn;

    @BindView(R.id.welcomeTextView)
    TextView mWelcomeTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);
        mSplashViewModel = ViewModelProviders.of(this, viewModelFactory).get(SplashViewModel.class);
        //init SplashViewModel
        mSplashViewModel.init();
        //Single are used to manage user click and single events messages
        suscribetoSingleEvents(mSplashViewModel);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AuthUtils.REQ_CODE && resultCode == RESULT_OK) {
            final String authCode = data.getStringExtra(AuthActivity.KEY_CODE);
            // splashPresenter.onSignInSuccess(authCode);  //todo -replace
            mSplashViewModel.onSignInSuccess(authCode);

        }
    }

    @OnClick(R.id.activity_login_btn)
    public void signIn() {
        mSplashViewModel.onSignInClicked();
    }

    private void suscribetoSingleEvents(SplashViewModel viewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.getSignInCommand().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void methdod) {
                goToAuthActivity();
                Toast.makeText(mApplication, "toast", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getGoToHomeCommand().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void methdod) {
                goToHome();
            }
        });

    }

    public void goToAuthActivity() {
        AuthUtils.openAuthActivity(SplashActivity.this);
    }

    public void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}