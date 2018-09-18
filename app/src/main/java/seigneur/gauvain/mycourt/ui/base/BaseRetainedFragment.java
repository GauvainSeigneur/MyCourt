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
import timber.log.Timber;

/**
 * Base {@link android.app.Fragment} class for every fragment in this application.
 * Based on this post: https://medium.com/inspace-labs-blog/efficient-and-bug-free-fragment-injection-in-android-mvp-applications-1245a3dd5a9
 */
public abstract class BaseRetainedFragment extends Fragment
    {

        private View mRootview; //child RootView reference
        private boolean mIsInjected = false; //check if dependencies are injected
        public Activity activity;

        /*
        *****************************************************************************************
        * Fragment Lifecycle
        *****************************************************************************************/
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                setRetainInstance(true); //allows presenterImpl to keep alive data though fragment lifecycle even onConfiguration thanks to FragmentScope set to presenter module
                /**
                 * DO NOT INJECT DEPENDENCIES HERE AS ACTIVITY MAY BE DESTROYED AND THE CONTEXT SET AS NULL
                 * OTHERWISE DEPENDENCIES WILL NOT BE GARBAGE COLLECTED and parent activity too
                 */
            }


        /**
         * View is inflating, bind views and define rootViews
         * @param inflater - methods to inflate fragment view
         * @param container -
         * @param savedInstanceState - bundle state
         * @return view inflated - mRootview
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater,container, savedInstanceState);
            mRootview = inflater.inflate(getFragmentLayout(), container, false);
            ButterKnife.bind(this, mRootview);
            return mRootview;
        }

        /**
         * View has been inflated, if dependency are injected call onViewInjected()*
         *
         * in child fragment, perform UI operation which may requires injected dependencies
         * @param view
         * @param savedInstanceState
         */
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState)
            {
                super.onViewCreated(view, savedInstanceState);
                if (!mIsInjected)
                {
                    mIsInjected = onInjectView();
                    if (mIsInjected)
                        onViewInjected(view, savedInstanceState);
                }
                else
                {
                    onViewInjected(view, savedInstanceState);
                }

            }

        /**
         * If activity is recreated, dependencies may have been recycled, so inject it again
         * @param savedInstanceState
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);
            Timber.d("onActivityCreated");
            if (!mIsInjected)
            {
                mIsInjected = onInjectView();
            }
        }


        @Override
        public void onDestroy()
            {
             super.onDestroy();
             mRootview=null; //Allows garbage collector to recycle view of the fragment
            }

        @Override
        public void onAttach(Context context)
        {
            super.onAttach(context);
            activity = getActivity();
        }

        /*
         *****************************************************************************************
         * Inner method
         *****************************************************************************************/

        /**
         * Get a reference of the RootView
         * @return
         */
        public View getmRootview()
            {
                return mRootview;
            }

        /**
         * Optional
         * Inject dependencies here when view and context are ready (inside OnViewCreated)
         * @return true if dependencies are injection, otherwise false
         * @throws IllegalStateException -
         */
        protected boolean onInjectView() throws IllegalStateException
        {
            // Return false by default.
           return false;
        }


        /**
         * Called when the fragment has been injected and the field injected can be initialized. This
         * will be called on {@link #onViewCreated(View, Bundle)} if {@link #onInjectView()} returned
         * true when executed on {@link #onCreate(Bundle)}, otherwise it will be called on
         * {@link #onActivityCreated(Bundle)} if {@link #onInjectView()} returned true right before.
         *
         * @param inSavedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         */
        @CallSuper
        protected void onViewInjected(View inRootView, Bundle inSavedInstanceState) {
            // Intentionally left empty.
        }

        /**
         * Every fragment has to inflate a layout in the onCreateView method. We have added this method to
         * avoid duplicate all the inflate code in every fragment. You only have to return the layout to
         * inflate in this method when extends BaseBottomNavFragment.
         */
        protected abstract int getFragmentLayout();


    }