package seigneur.gauvain.mycourt.ui.main.view;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.main.presenter.MainPresenterModule;
import seigneur.gauvain.mycourt.ui.shotDraft.view.ShotDraftFragment;
import seigneur.gauvain.mycourt.ui.shotDraft.view.ShotDraftFragmentModule;
import seigneur.gauvain.mycourt.ui.shots.view.ShotsFragment;
import seigneur.gauvain.mycourt.ui.shots.view.ShotsFragmentModule;
import seigneur.gauvain.mycourt.ui.user.view.UserFragment;
import seigneur.gauvain.mycourt.ui.user.view.UserFragmentModule;

/**
 * Provides main activity dependencies.
 */
@Module(includes={
        MainPresenterModule.class
})
public abstract class MainActivityModule {
    /**
     * Binds MainView into MainActivity
     * @param mainActivity
     * @return
     */
    @Binds
    @PerActivity
    abstract MainView mainView(MainActivity mainActivity);

    /**
     * Provides the injector for the {@link ShotsFragment}, which has access to the dependencies
     * provided by this activity and application instance (singleton scoped objects).
     */
    @PerFragment
    @ContributesAndroidInjector(modules = ShotsFragmentModule.class)
    abstract ShotsFragment shotsFragmentInjector();

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