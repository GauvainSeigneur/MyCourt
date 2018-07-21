package seigneur.gauvain.mycourt.ui.splash.view;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.splash.presenter.SplashPresenterModule;

/**
 * Provides SplashActivity dependencies.
 */
@Module(includes = SplashPresenterModule.class)
public abstract class SplashActivityModule {

    @Binds
    @PerActivity
    abstract Activity activity(SplashActivity splashActivity);
    /**
     * Binds splashView into SplashActivity
     * @param splashActivity
     * @return splashView
     */
    @Binds
    @PerActivity
    abstract SplashView splashView(SplashActivity splashActivity);

}