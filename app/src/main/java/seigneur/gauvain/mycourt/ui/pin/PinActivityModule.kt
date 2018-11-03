package seigneur.gauvain.mycourt.ui.pin

import android.app.Activity

import dagger.Binds
import dagger.Module
import seigneur.gauvain.mycourt.di.scope.PerActivity

/**
 * Provides activity dependencies.
 */
@Module
abstract class PinActivityModule {

    @Binds
    @PerActivity
    internal abstract fun activity(pinActivity: PinActivity): Activity


}