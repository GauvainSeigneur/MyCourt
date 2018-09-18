package seigneur.gauvain.mycourt.ui.splash.presenter;

import seigneur.gauvain.mycourt.ui.base.mvp.BaseMVPView;
import seigneur.gauvain.mycourt.ui.base.mvp.BasePresenter;

public interface SplashPresenter<V extends BaseMVPView> extends BasePresenter<V> {

    void onViewReady();

    void onSignInClicked();

    void onSignInSuccess(String authCode);

}


