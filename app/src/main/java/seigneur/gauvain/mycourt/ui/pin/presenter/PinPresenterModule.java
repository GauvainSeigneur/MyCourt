package seigneur.gauvain.mycourt.ui.pin.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.pin.view.PinView;

@Module
public abstract class PinPresenterModule {
    @Binds
    @PerActivity
    abstract PinPresenter<PinView> pinPresenter(PinPresenterImpl<PinView> pinPresenterImpl);
}
