package seigneur.gauvain.mycourt.ui.base;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;

/**
 * Provides main activity dependencies.
 */
@Module()
public abstract class BaseActivityModule {
    /**
     * @param baseActivity the activity
     * @return the activity
     */
    @Binds
    @PerActivity
    abstract Activity activity(BaseActivity baseActivity);
}