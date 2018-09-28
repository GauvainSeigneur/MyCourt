package seigneur.gauvain.mycourt.ui.splash;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
/**
 * Provides SplashActivity dependencies.
 */
@Module
public abstract class SplashActivityModule {

    @Binds
    @PerActivity
    abstract Activity activity(SplashActivity splashActivity);


}