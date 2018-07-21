package seigneur.gauvain.mycourt.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class BaseFragment extends Fragment {
    public View rootview;
    public Activity activity;
    public boolean isNewInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //first time created
        // Check if it's a new view
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //issue :fragment instance lost on Activity Resume after some time inactive : app crash
        //solution : put the view variable inside class and inflating it as new only if the view instance is null or otherwise use the one created before - (todo-to be tested : DOESN'T WORK AS EXPECTED)
        //source solution :  https://stackoverflow.com/questions/18428152/stop-fragment-from-being-recreated-after-resume
        if (rootview == null) {
            rootview = inflater.inflate(getFragmentLayout(), container, false );
            onCreateView(rootview,savedInstanceState);
        }
        return rootview;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    /**
     * Every fragment has to inflate a layout in the onCreateView method. We have added this method to
     * avoid duplicate all the inflate code in every fragment. You only have to return the layout to
     * inflate in this method when extends BaseFragment.
     */
    protected abstract int getFragmentLayout();

}