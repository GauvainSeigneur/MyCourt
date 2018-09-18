package seigneur.gauvain.mycourt.ui.main.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.ui.main.view.MainView;

@Module
public abstract class MainPresenterModule {
    @Binds
    @PerActivity
    abstract MainPresenter<MainView> mainPresenter(
            MainPresenterImpl<MainView> mainPresenterImpl);
}
