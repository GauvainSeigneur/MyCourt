package seigneur.gauvain.mycourt.ui.main

import android.app.Activity

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import seigneur.gauvain.mycourt.di.scope.PerActivity
import seigneur.gauvain.mycourt.di.scope.PerFragment
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftFragment
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftFragmentModule
import seigneur.gauvain.mycourt.ui.shots.ShotsFragmentModule
import seigneur.gauvain.mycourt.ui.shots.ShotsFragment
import seigneur.gauvain.mycourt.ui.user.UserFragment
import seigneur.gauvain.mycourt.ui.user.UserFragmentModule

/**
 * Provides main activity dependencies.
 */
@Module
abstract class MainActivityModule {
    /**
     * Provides the injector for the [ShotsFragment], which has access to the dependencies
     * provided by this activity and application instance (singleton scoped objects).
     */
    @PerFragment
    @ContributesAndroidInjector(modules = arrayOf(ShotsFragmentModule::class))
    internal abstract fun shotsFragmentInjector(): ShotsFragment

    /**
     * Provides the injector for the [ShotDraftFragment], which has access to the dependencies
     * provided by this activity and application instance (singleton scoped objects).
     */
    @PerFragment
    @ContributesAndroidInjector(modules = arrayOf(ShotDraftFragmentModule::class))
    internal abstract fun postFragmentInjector(): ShotDraftFragment

    /**
     * Provides the injector for the [UserFragment], which has access to the dependencies
     * provided by this activity and application instance (singleton scoped objects).
     */
    @PerFragment
    @ContributesAndroidInjector(modules = arrayOf(UserFragmentModule::class))
    internal abstract fun userFragmentInjector(): UserFragment

    /**
     * @param mainActivity the activity
     * @return the activity
     */
    @Binds
    @PerActivity
    internal abstract fun activity(mainActivity: MainActivity): Activity
}