package seigneur.gauvain.mycourt.ui.shotDraft.view;

import android.support.v4.app.Fragment;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.shotDraft.presenter.ShotDraftPresenterModule;


@Module(includes={
        ShotDraftPresenterModule.class
})
public abstract class ShotDraftFragmentModule {
    /**
     *provide a concrete implementation of {@link Fragment}
     *
     * @param postFragment is the ShotsFragment
     * @return the fragment
     */
    @Binds
    @PerFragment
    abstract Fragment fragment(ShotDraftFragment postFragment);


    /**
     * Binds ShotsView into ShotsFragment
     * @param postFragment
     * @return
     */
    @Binds
    @PerFragment
    abstract ShotDraftView postView(ShotDraftFragment postFragment);

}