package seigneur.gauvain.mycourt.ui.user.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.di.scope.PerFragment;

@Module
public abstract class UserPresenterModule {
    @Binds
    @PerFragment
    abstract UserPresenter userPresenter(UserPresenterImpl userPresenterImpl);
}
