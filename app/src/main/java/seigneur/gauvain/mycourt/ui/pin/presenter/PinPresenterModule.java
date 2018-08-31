package seigneur.gauvain.mycourt.ui.pin.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;

@Module
public abstract class PinPresenterModule {
    @Binds
    @PerActivity
    abstract PinPresenter pinPresenter(PinPresenterImpl pinPresenterImpl);
}
