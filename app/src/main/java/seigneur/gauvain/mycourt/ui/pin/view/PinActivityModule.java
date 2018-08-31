package seigneur.gauvain.mycourt.ui.pin.view;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.pin.presenter.PinPresenterModule;

/**
 * Provides activity dependencies.
 */
@Module(includes={
        PinPresenterModule.class
})
public abstract class PinActivityModule {

    /**
     * Binds PinView into MainActivity
     * @param pinActivity
     * @return implementation of PinView
     */
    @Binds
    @PerActivity
    abstract PinView pinView(PinActivity pinActivity);


}