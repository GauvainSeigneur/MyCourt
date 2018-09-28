package seigneur.gauvain.mycourt.ui.shotDetail;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;

/**
 * Provides dependencies.
 */
@Module
public abstract class ShotDetailActivityModule {
    /**
     *
     * @param mShotDetailActivity
     * @return
     */
    @Binds
    @PerActivity
    abstract Activity activity(ShotDetailActivity mShotDetailActivity);



}