package seigneur.gauvain.mycourt.ui.main;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftFragment;
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftFragmentModule;
import seigneur.gauvain.mycourt.ui.shots.ShotsFragmentModule;
import seigneur.gauvain.mycourt.ui.shots.ShotsFragment;
import seigneur.gauvain.mycourt.ui.user.UserFragment;
import seigneur.gauvain.mycourt.ui.user.UserFragmentModule;

/**
 * Provides main activity dependencies.
 */
@Module
public abstract class MainActivityModule {
    /**
     * Provides the injector for the {@link ShotsFragment}, which has access to the dependencies
     * provided by this activity and application instance (singleton scoped objects).
     */
    @PerFragment
    @ContributesAndroidInjector(modules = ShotsFragmentModule.class)
    abstract ShotsFragment sshotsFragmentInjector();

    /**
     * Provides the injector for the {@link ShotDraftFragment}, which has access to the dependencies
     * provided by this activity and application instance (singleton scoped objects).
     */
    @PerFragment
    @ContributesAndroidInjector(modules = ShotDraftFragmentModule.class)
    abstract ShotDraftFragment postFragmentInjector();

    /**
     * Provides the injector for the {@link UserFragment}, which has access to the dependencies
     * provided by this activity and application instance (singleton scoped objects).
     */
    @PerFragment
    @ContributesAndroidInjector(modules = UserFragmentModule.class)
    abstract UserFragment userFragmentInjector();

    /**
     * @param mainActivity the activity
     * @return the activity
     */
    @Binds
    @PerActivity
    abstract Activity activity(MainActivity mainActivity);
}