package seigneur.gauvain.mycourt.ui.user


import android.support.v4.app.Fragment

import dagger.Binds
import dagger.Module
import seigneur.gauvain.mycourt.di.scope.PerFragment

/**
 * Provides activity dependencies.
 */
@Module
abstract class UserFragmentModule {

    /**
     * provide a concrete implementation of [Fragment]
     *
     * @param userFragment is the UserFragment
     * @return the fragment
     */
    @Binds
    @PerFragment
    internal abstract fun fragment(userFragment: UserFragment): Fragment


}