package seigneur.gauvain.mycourt.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import seigneur.gauvain.mycourt.ui.pin.PinActivity
import seigneur.gauvain.mycourt.ui.pin.PinActivityModule
import seigneur.gauvain.mycourt.ui.main.MainActivity
import seigneur.gauvain.mycourt.ui.main.MainActivityModule
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.base.BaseActivityModule
import seigneur.gauvain.mycourt.ui.shotDetail.ShotDetailActivity
import seigneur.gauvain.mycourt.ui.shotDetail.ShotDetailActivityModule
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivityModule
import seigneur.gauvain.mycourt.ui.splash.SplashActivityModule
import seigneur.gauvain.mycourt.ui.splash.SplashActivity
import seigneur.gauvain.mycourt.di.scope.PerActivity

/**
 * Created by Gauvain on 26/03/2018.
 * Module which allows activities to have access to injected dependencies
 */
@Module(includes = [AndroidSupportInjectionModule::class, ViewModelModule::class])
abstract class AppModule {
    /**
     * Provides the injector for the [BaseActivity], which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = [BaseActivityModule::class])
    internal abstract fun baseActivityInjector(): BaseActivity

    /**
     * Provides the injector for the [MainActivity], which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    internal abstract fun mainActivityInjector(): MainActivity

    /**
     * Provides the injector for the [ShotDetailActivity], which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = [ShotDetailActivityModule::class])
    internal abstract fun shotDetailActivityInjector(): ShotDetailActivity

    /**
     * Provides the injector for the [EditShotActivity], which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = [EditShotActivityModule::class])
    internal abstract fun createShotActivityInjector(): EditShotActivity

    /**
     * Provides the injector for the [PinActivity], which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = [PinActivityModule::class])
    internal abstract fun createPinActivityInjector(): PinActivity

    /**
     * Provides the injector for the [SplashActivity], which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = [SplashActivityModule::class])
    internal abstract fun splashActivityInjector(): SplashActivity

}