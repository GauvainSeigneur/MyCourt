package seigneur.gauvain.mycourt.ui.shotDetail.view;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.shotDetail.presenter.ShotDetailPresenterModule;

/**
 * Provides dependencies.
 */
@Module(includes={
        ShotDetailPresenterModule.class
})
public abstract class ShotDetailActivityModule {
    /**
     *
     * @param mShotDetailActivity
     * @return
     */
    @Binds
    @PerActivity
    abstract Activity activity(ShotDetailActivity mShotDetailActivity);

    /**
     *
     * @param shotDetailActivity
     * @return
     */
    @Binds
    @PerActivity
    abstract ShotDetailView shotDetailView(ShotDetailActivity shotDetailActivity);


}