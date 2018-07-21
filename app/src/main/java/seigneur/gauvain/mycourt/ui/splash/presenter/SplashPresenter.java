package seigneur.gauvain.mycourt.ui.splash.presenter;

import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface SplashPresenter extends BasePresenter {

    void onSignInClicked();

    void onSignInSuccess(String authCode);

}


