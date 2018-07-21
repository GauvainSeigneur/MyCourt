package seigneur.gauvain.mycourt.ui.main.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
@Module
public abstract class MainPresenterModule {
    @Binds
    @PerActivity
    abstract MainPresenter mainPresenter(MainPresenterImpl mainPresenterImpl);
}
