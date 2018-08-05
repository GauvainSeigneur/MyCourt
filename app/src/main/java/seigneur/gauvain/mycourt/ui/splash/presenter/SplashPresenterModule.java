package seigneur.gauvain.mycourt.ui.splash.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;

@Module
public abstract class SplashPresenterModule {
    @Binds
    @PerActivity
    abstract SplashPresenter splashPresenter(SplashPresenterImpl splashPresenterImpl);
}
