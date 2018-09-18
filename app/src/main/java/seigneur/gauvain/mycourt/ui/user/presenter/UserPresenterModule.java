package seigneur.gauvain.mycourt.ui.user.presenter;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerActivity;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.user.view.UserViewTest;

@Module
public abstract class UserPresenterModule {
    /*@Binds
    @PerFragment
    abstract UserPresenter userPresenter(UserPresenterImpl userPresenterImpl);*/

    @Binds
    @PerFragment
    abstract UserPresenterTest<UserViewTest> userPresenterTest(UserPresenterImplTest<UserViewTest>  userPresenterImplTest);
}
