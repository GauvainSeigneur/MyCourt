package seigneur.gauvain.mycourt.ui.splash.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import seigneur.gauvain.mycourt.data.api.AuthUtils;
import seigneur.gauvain.mycourt.data.repository.TokenRepository;
import seigneur.gauvain.mycourt.data.repository.UserRepository;
import seigneur.gauvain.mycourt.ui.AuthActivity;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.ui.main.view.MainActivity;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.ui.pin.view.PinActivity;
import seigneur.gauvain.mycourt.ui.splash.presenter.SplashPresenter;
import timber.log.Timber;

public class SplashActivity extends BaseActivity implements SplashView {

    @Inject
    SplashPresenter<SplashView> splashPresenter;

    @BindView(R.id.activity_login_btn)
    Button loginBtn;
    @BindView(R.id.welcomeTextView) TextView mWelcomeTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);
        splashPresenter.onAttach(this);
        splashPresenter.onViewReady();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        splashPresenter.onDetach();
    }


    @OnClick(R.id.activity_login_btn)
    public void signIn() {
        splashPresenter.onSignInClicked();
    }

    @Override
    public void goToAuthActivity() {
        AuthUtils.openAuthActivity(SplashActivity.this);
    }

    @Override
    public void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AuthUtils.REQ_CODE && resultCode == RESULT_OK) {
            final String authCode = data.getStringExtra(AuthActivity.KEY_CODE);
            splashPresenter.onSignInSuccess(authCode);
        }
    }

}