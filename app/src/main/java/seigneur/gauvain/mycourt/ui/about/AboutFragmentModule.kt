package seigneur.gauvain.mycourt.ui.about


import androidx.fragment.app.Fragment

import dagger.Binds
import dagger.Module
import seigneur.gauvain.mycourt.di.scope.PerFragment

/**
 * Provides activity dependencies.
 */
@Module
abstract class AboutFragmentModule {

    /**
     * provide a concrete implementation of [Fragment]
     *
     * @param userFragment is the UserFragment
     * @return the fragment
     */
    @Binds
    @PerFragment
    internal abstract fun fragment(aboutFragment: AboutFragment): androidx.fragment.app.Fragment


}