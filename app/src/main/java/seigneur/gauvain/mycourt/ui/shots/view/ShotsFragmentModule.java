package seigneur.gauvain.mycourt.ui.shots.view;

import android.support.v4.app.Fragment;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.shots.presenter.ShotsPresenterModule;


@Module(includes={
        ShotsPresenterModule.class
})
public abstract class ShotsFragmentModule {


    /**
     *provide a concrete implementation of {@link Fragment}
     *
     * @param shotsFragment is the ShotsFragment
     * @return the fragment
     */
    @Binds
    @PerFragment
    abstract Fragment fragment(ShotsFragment shotsFragment);


    /**
     * Binds ShotsView into ShotsFragment
     * @param shotsFragment
     * @return
     */
    @Binds
    @PerFragment
    abstract ShotsView shotsView(ShotsFragment shotsFragment);

}