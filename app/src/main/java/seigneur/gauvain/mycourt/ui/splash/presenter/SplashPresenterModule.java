package seigneur.gauvain.mycourt.ui.splash.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.splash.view.SplashActivityModule;
import seigneur.gauvain.mycourt.ui.splash.view.SplashView;

@Module
public abstract class SplashPresenterModule {
    @Binds
    @PerActivity
    abstract SplashPresenter<SplashView> splashPresenter(SplashPresenterImpl<SplashView> splashPresenterImpl);
}
