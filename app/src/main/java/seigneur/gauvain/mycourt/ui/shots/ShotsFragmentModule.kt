package seigneur.gauvain.mycourt.ui.shots

import androidx.fragment.app.Fragment

import dagger.Binds
import dagger.Module
import seigneur.gauvain.mycourt.di.scope.PerFragment

@Module
abstract class ShotsFragmentModule {
    @Binds
    @PerFragment
    internal abstract fun fragment(shotsFragment: ShotsFragment): androidx.fragment.app.Fragment

}