package seigneur.gauvain.mycourt.ui.about

import android.arch.lifecycle.ViewModel
import javax.inject.Inject

import io.reactivex.disposables.CompositeDisposable

class AboutViewModel @Inject
constructor() : ViewModel() {

    private val mCompositeDisposable = CompositeDisposable()

    public override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

    /*
    *********************************************************************************************
    * PUBLIC METHODS CALLED IN Fragment
    *********************************************************************************************/
    fun init() {}


}
