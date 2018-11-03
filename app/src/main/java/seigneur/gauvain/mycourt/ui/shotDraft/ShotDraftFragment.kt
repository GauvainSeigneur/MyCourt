package seigneur.gauvain.mycourt.ui.shotDraft

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import java.util.ArrayList

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import dagger.android.support.AndroidSupportInjection
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.ui.base.BaseFragment
import seigneur.gauvain.mycourt.ui.main.MainViewModel
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity
import timber.log.Timber
import java.util.function.Consumer


/**
 * Created by gse on 22/11/2017.
 */
class ShotDraftFragment : BaseFragment(), Toolbar.OnMenuItemClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mApplication: Application

    private val mShotDraftViewModel: ShotDraftViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ShotDraftViewModel::class.java)
    }

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.swipe_refresh_drafts)
    lateinit var mRefreshDraftLayout: SwipeRefreshLayout

    @BindView(R.id.rv_shot_draft)
    lateinit var shotDraftRV: RecyclerView

    private var mcallabck: ShotDraftListCallback? = null
    private var mShotDraftsListAdapter: ShotDraftsListAdapter?=null
    private var shotDraftsSaved: MutableList<Draft> = ArrayList()

    override val fragmentLayout: Int
        get() = R.layout.fragment_shot_draft

    /*
    ************************************************************************************
    *  Fragment lifecycle
    ************************************************************************************/
    override fun onAttach(activity: Activity?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this)
        }
        super.onAttach(activity)
    }

    override fun onAttach(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this)
        }
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        //provide ViewModel
        mShotDraftViewModel.fetchShotDrafts()
        mcallabck = object : ShotDraftListCallback {
            override fun onShotDraftClicked(shotDraft: Draft, position: Int) {
                mShotDraftViewModel.onShotDraftClicked(shotDraft, position)
            }
        }
        mShotDraftsListAdapter = ShotDraftsListAdapter(context!!, shotDraftsSaved, mcallabck!!)
    }

    override fun onCreateView(inRootView: View, inSavedInstanceState: Bundle?) {
        Timber.d("onCreateView")
        toolbar.inflateMenu(R.menu.menu_shot_detail)
        toolbar.setOnMenuItemClickListener(this)
        shotDraftRV.layoutManager = GridLayoutManager(context, 2)
        shotDraftRV.adapter = mShotDraftsListAdapter
        mRefreshDraftLayout.setOnRefreshListener { mShotDraftViewModel.onRefresh(true) }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //listen livedata
        subscribeToSingleEvents(mShotDraftViewModel)
        listenLiveData(mShotDraftViewModel)
    }

    private fun subscribeToSingleEvents(viewModel: ShotDraftViewModel) {
        viewModel.dbChanged().observe(
                this,
                Observer<Void> {
                    _ ->
                    Timber.d("db has changed")
                    Toast.makeText(mApplication, "db has changed", Toast.LENGTH_SHORT).show()
                }

        )

        viewModel.itemClickedEvent.observe(
                this,
                Observer<Draft> { _ ->
                    val intent = Intent(activity, EditShotActivity::class.java)
                    startActivity(intent)
                }
        )


    }

    private fun listenLiveData(shotDraftViewModel: ShotDraftViewModel) {
        mShotDraftViewModel.drafts
                .observe(
                        this,
                        Observer { drafts ->
                            if (drafts != null) {
                                showEmptyView(false)
                                showDraftList(drafts, false)//todo - fix this
                            } else {
                                showEmptyView(true)
                            }
                        }
                )
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume called")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause called")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_settings -> {
                Toast.makeText(mApplication, "yeah!", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    fun stopRefresh() {
        // if (mRefreshDraftLayout.isRefreshing())
        mRefreshDraftLayout.isRefreshing = false
    }

    fun showDraftList(shotDraft: List<Draft>?, isRefreshing: Boolean) {
        if (isRefreshing)
            mShotDraftsListAdapter!!.clear() //todo - use diffutils instead of this, is not perform
        mShotDraftsListAdapter!!.clear() //TODO - MANAGE THIS EVENT MORE PROPERLY

        shotDraftsSaved.addAll(shotDraft!!)
        mShotDraftsListAdapter!!.notifyDataSetChanged()
    }

    fun showEmptyView(isVisible: Boolean) {
        if (isVisible)
            Toast.makeText(mApplication, "empty view", Toast.LENGTH_SHORT).show()
    }

    fun goToShotEdition() {
        val intent = Intent(activity, EditShotActivity::class.java)
        startActivity(intent)
    }


}
