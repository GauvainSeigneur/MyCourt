package seigneur.gauvain.mycourt.di.modules;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import seigneur.gauvain.mycourt.ui.shotEdition.view.EditShotActivity;
import seigneur.gauvain.mycourt.ui.shotEdition.view.EditShotActivityModule;
import seigneur.gauvain.mycourt.ui.main.view.MainActivity;
import seigneur.gauvain.mycourt.ui.main.view.MainActivityModule;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.ui.base.BaseActivityModule;
import seigneur.gauvain.mycourt.ui.shotDetail.view.ShotDetailActivity;
import seigneur.gauvain.mycourt.ui.shotDetail.view.ShotDetailActivityModule;
import seigneur.gauvain.mycourt.ui.splash.view.SplashActivityModule;
import seigneur.gauvain.mycourt.ui.splash.view.SplashActivity;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
/**
 * Created by gse on 26/03/2018.
 */
@Module(includes = {
        AndroidSupportInjectionModule.class
})
public abstract class AppModule {
    /**
     * Provides the injector for the {@link BaseActivity}, which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = {
            BaseActivityModule.class
    })
    abstract BaseActivity baseActivityInjector();

    /**
     * Provides the injector for the {@link SplashActivity}, which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = SplashActivityModule.class)
    abstract SplashActivity splashActivityInjector();

    /**
     * Provides the injector for the {@link MainActivity}, which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = {
            MainActivityModule.class
    })
    abstract MainActivity mainActivityInjector();

    /**
     * Provides the injector for the {@link ShotDetailActivity}, which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = {
            ShotDetailActivityModule.class
    })
    abstract ShotDetailActivity shotDetailActivityInjector();

    /**
     * Provides the injector for the {@link EditShotActivity}, which has access to the dependencies
     * provided by this application instance (singleton scoped objects).
     */
    @PerActivity
    @ContributesAndroidInjector(modules = {
            EditShotActivityModule.class
    })
    abstract EditShotActivity createShotActivityInjector();

}