package seigneur.gauvain.mycourt.ui.splash

import android.app.Activity

import dagger.Binds
import dagger.Module
import seigneur.gauvain.mycourt.di.scope.PerActivity

/**
 * Provides SplashActivity dependencies.
 */
@Module
abstract class SplashActivityModule {

    @Binds
    @PerActivity
    internal abstract fun activity(splashActivity: SplashActivity): Activity


}