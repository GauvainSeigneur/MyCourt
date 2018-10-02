package seigneur.gauvain.mycourt.ui.pin;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;

/**
 * Provides activity dependencies.
 */
@Module
public abstract class PinActivityModule {

    @Binds
    @PerActivity
    abstract Activity activity(PinActivity pinActivity);


}