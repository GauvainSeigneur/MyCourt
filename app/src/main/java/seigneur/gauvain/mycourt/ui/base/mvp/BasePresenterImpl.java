package seigneur.gauvain.mycourt.ui.base.mvp;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * Base presenter to not duplicate some global methods
 * ONLY UI, NOT DI
 */
public class BasePresenterImpl<V extends  BaseMVPView>
        implements BasePresenter<V>, LifecycleObserver {

    private LifecycleObserver mLifecycleObserver;
    private CompositeDisposable mCompositeDisposable;
    private V mBaseMVPView;

    @Override
    //@OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAttach(V view) {
        mBaseMVPView =view;
        if (mBaseMVPView instanceof LifecycleOwner && mLifecycleObserver == null) {
            ((LifecycleOwner) mBaseMVPView).getLifecycle().addObserver(this);
            Timber.d("addObserver");
            mLifecycleObserver = this;
        }
        if (mCompositeDisposable == null)
            mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDetach() {
        // Clean up any no-longer-use resources here
//        ((LifecycleOwner) mBaseMVPView).getLifecycle().removeObserver(this);
        mLifecycleObserver = null;
        mBaseMVPView = null;
        mCompositeDisposable.dispose();
    }


    public V getMvpView() {
        return mBaseMVPView;
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }
}

