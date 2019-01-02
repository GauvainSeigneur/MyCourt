package seigneur.gauvain.mycourt.ui.about

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.text.Html
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

import java.util.ArrayList

import javax.inject.Inject

import butterknife.BindView
import dagger.android.support.AndroidSupportInjection
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.User
import seigneur.gauvain.mycourt.ui.base.BaseFragment
import timber.log.Timber

import seigneur.gauvain.mycourt.utils.MathUtils.convertPixelsToDp

/**
 * Created by gse on 22/11/2017.
 */
class AboutFragment : BaseFragment() {

    @Inject
    lateinit var mApplication: Application

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mAboutViewModel: AboutViewModel by lazy {
         ViewModelProviders.of(this, viewModelFactory).get(AboutViewModel::class.java)
    }

    /*
    ************************************************************************************
    *  Fragment lifecycle
    ************************************************************************************/
    override fun onCreateView(inRootView: View, inSavedInstanceState: Bundle?) {
        mAboutViewModel.init()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //listen LiveData
        subscribeToLiveData(mAboutViewModel)
    }

    override val fragmentLayout: Int
        get() = R.layout.fragment_about
    /*
    ************************************************************************************
    * VIEWMODEL SUBSCRIPTION
    ************************************************************************************/
    private fun subscribeToLiveData(viewModel: AboutViewModel) {
    }

}
