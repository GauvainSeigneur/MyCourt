package seigneur.gauvain.mycourt.ui.user.view;


import android.support.v4.app.Fragment;

import dagger.Binds;
import dagger.Module;
import seigneur.gauvain.mycourt.di.scope.PerFragment;
import seigneur.gauvain.mycourt.ui.user.presenter.UserPresenterModule;

/**
 * Provides activity dependencies.
 */
@Module(includes={
        UserPresenterModule.class
})
public abstract class UserFragmentModule {

    /**
     *provide a concrete implementation of {@link Fragment}
     *
     * @param userFragment is the UserFragment
     * @return the fragment
     */
    @Binds
    @PerFragment
    abstract Fragment fragment(UserFragment userFragment);


    //as we do not injetc View inside presenter, we don't need to inject it.. thought the fragment
    /*@Binds
    @PerFragment
    abstract UserView userView(UserFragment userFragment);*/


}