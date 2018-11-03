package seigneur.gauvain.mycourt.ui.shotDraft

import android.support.v4.app.Fragment

import dagger.Binds
import dagger.Module
import seigneur.gauvain.mycourt.di.scope.PerFragment


@Module
abstract class ShotDraftFragmentModule {
    /**
     * provide a concrete implementation of [Fragment]
     *
     * @param postFragment is the ShotsFragment
     * @return the fragment
     */
    @Binds
    @PerFragment
    internal abstract fun fragment(postFragment: ShotDraftFragment): Fragment


}