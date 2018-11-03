package seigneur.gauvain.mycourt.ui.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import butterknife.ButterKnife
import butterknife.Unbinder

/**
 * Base fragment which allows to not duplicate some methods in child
 * Fragment - dedicated to UI not DI
 */
abstract class BaseFragment : Fragment() {
    lateinit var mRootview: View

    val parentActivity: FragmentActivity? by lazy {
        this.activity
    }

    lateinit var mUnbinder: Unbinder

    /**
     * Every fragment has to inflate a layout in the onCreateView method. We have added this method to
     * avoid duplicate all the inflate code in every fragment. You only have to return the layout to
     * inflate in this method when extends BaseFragment.
     */
    protected abstract val fragmentLayout: Int


    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootview = inflater.inflate(fragmentLayout, container, false)
        mUnbinder = ButterKnife.bind(this, mRootview)
        onCreateView(mRootview, savedInstanceState)
        return mRootview

    }

    /**
     * method to be overriden.
     * In this method, root view is already inflated and one can use below view getters
     * @param inRootView
     * @param inSavedInstanceState
     */
    open fun onCreateView(inRootView: View, inSavedInstanceState: Bundle?) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        //mUnbinder.unbind();
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

