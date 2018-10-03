package seigneur.gauvain.mycourt.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Base fragment which allows to not duplicate some methods in child
 * Fragment - dedicated to UI not DI
 */
public abstract class BaseFragment extends Fragment {
    public View mRootview;
    public Activity activity;
    public Unbinder mUnbinder;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootview = inflater.inflate(getFragmentLayout(), container, false );
        mUnbinder = ButterKnife.bind(this, mRootview);
        onCreateView(mRootview,savedInstanceState);
        return mRootview;

    }

    /**
     method to be overriden.
     In this method, root view is already inflated and one can use below view getters
     @param inRootView
     @param inSavedInstanceState
     */
    public void onCreateView(View inRootView, Bundle inSavedInstanceState){

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mUnbinder.unbind();
    }

    /**
     * Every fragment has to inflate a layout in the onCreateView method. We have added this method to
     * avoid duplicate all the inflate code in every fragment. You only have to return the layout to
     * inflate in this method when extends BaseFragment.
     */
    protected abstract int getFragmentLayout();

}

