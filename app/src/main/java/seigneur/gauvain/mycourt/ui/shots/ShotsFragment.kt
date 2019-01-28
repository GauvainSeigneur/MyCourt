package seigneur.gauvain.mycourt.ui.shots

import android.app.Activity
import android.app.ActivityOptions
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import butterknife.*
import com.google.android.material.appbar.AppBarLayout

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.ui.base.BaseFragment
import seigneur.gauvain.mycourt.ui.shotDetail.ShotDetailActivity
import seigneur.gauvain.mycourt.ui.shots.list.adapter.ShotItemCallback
import seigneur.gauvain.mycourt.ui.shots.list.adapter.ShotListAdapter
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState
import seigneur.gauvain.mycourt.ui.shots.list.data.Status
import timber.log.Timber


/**
 * Created by gse on 22/11/2017.
 */
class ShotsFragment : BaseFragment(), ShotItemCallback {

    @BindView(R.id.appbar_layout)
    lateinit var mAppbar:AppBarLayout

    @BindView(R.id.usersSwipeRefreshLayout)
    lateinit var mSwipeRefreshLayout:SwipeRefreshLayout

    @BindView(R.id.rvShots)
    lateinit var mRvShots: RecyclerView

    @BindView(R.id.globalNetworkState)
    lateinit var globalNetworkState: LinearLayout

    @BindView(R.id.errorMessageTextView)
    lateinit var errorMessageTextView: TextView

    @BindView(R.id.retryLoadingButton)
    lateinit var retryLoadingButton: Button

    @BindView(R.id.loadingProgressBar)
    lateinit var loadingProgressBar: ProgressBar

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val shotsViewModel: ShotsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ShotsViewModel::class.java)
    }

    private val shotListAdapter: ShotListAdapter by lazy {
        ShotListAdapter(this)
    }

    lateinit var mGridLayoutManager: GridLayoutManager

    override val fragmentLayout: Int
        get() = R.layout.fragment_shots

    /*
    ************************************************************************************
    *  Fragment lifecycle
    ************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        shotsViewModel.init()
    }

    override fun onCreateView(inRootView: View, inSavedInstanceState: Bundle?) {
        Timber.d("onCreateView")
        initAdapter()
        initSwipeToRefresh()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //listen livedata
        subscribeToSingleEvent(shotsViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
       // mUnbinder.unbind()
    }

    private fun initAdapter() {
        if (mRvShots.layoutManager==null && mRvShots.adapter==null) {
            mGridLayoutManager = GridLayoutManager(context, 2)
            mGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    when (shotListAdapter.getItemViewType(position)) {
                        ShotListAdapter.ITEM -> if (position == 0) return 2 else return 1
                        ShotListAdapter.LOADING -> return 2
                        else -> return 1
                    }
                }
            }
            mRvShots.layoutManager =  mGridLayoutManager
            mRvShots.adapter = shotListAdapter

        }

        shotsViewModel.shotList?.observe(
                this, Observer<PagedList<Shot>> {
            shotListAdapter.submitList(it)

        })

        shotsViewModel.networkState.observe(this, Observer<NetworkState> {
            shotListAdapter.setNetworkState(it!!)
            })

        mRvShots.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //todo - pass this data in view model to not loose the current state and scroll
                mAppbar.isSelected = mRvShots.canScrollVertically(-1)
            }
        })
    }

    /**
     * Init swipe to refresh and enable pull to refresh only when there are items in the adapter
     */
    private fun initSwipeToRefresh() {
        shotsViewModel.refreshState.observe(this,
                Observer<NetworkState> { networkState ->
                    if (networkState != null) {
                        if (shotListAdapter.currentList != null) {
                            if (shotListAdapter.currentList!!.size > 0) {
                                mSwipeRefreshLayout.isRefreshing = networkState.status == NetworkState.LOADING.status

                                setInitialLoadingState(networkState)
                            } else {
                                setInitialLoadingState(networkState)
                                if (mSwipeRefreshLayout.isRefreshing) {
                                    mSwipeRefreshLayout.isRefreshing=false
                                }
                            }
                        } else {
                            setInitialLoadingState(networkState)
                            if (mSwipeRefreshLayout.isRefreshing) {
                                mSwipeRefreshLayout.isRefreshing=false
                            }
                        }
                    }
                })
        mSwipeRefreshLayout.setOnRefreshListener {
            shotsViewModel.refresh()
        }
    }

    private fun subscribeToSingleEvent(shotsViewModel: ShotsViewModel) {
        shotsViewModel.shotClickEvent.observe(
                this,
                Observer<Int>
                { position ->
                    var options: ActivityOptions? = null
                    val i = Intent(activity, ShotDetailActivity::class.java)
                    context!!.startActivity(i)
                    //deactivated because gif not loaded with shared transition
                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        options = ActivityOptions.makeSceneTransitionAnimation(activity as Activity?,
                                mGridLayoutManager.findViewByPosition(position!!),
                                activity!!.getString(R.string.shot_transition_name))
                        context!!.startActivity(i, options!!.toBundle())
                    }*/
                }
        )
    }

    /**
     * Show the current network state for the first load when the user list
     * in the adapter is empty and disable swipe to scroll at the first loading
     *
     * @param networkState the new network state
     */
    private fun setInitialLoadingState(networkState: NetworkState) {
        //error message
        errorMessageTextView.visibility = if (!networkState.message.isEmpty()) View.VISIBLE else View.GONE
        errorMessageTextView.text = networkState.message
        //Visibility according to state
        retryLoadingButton.visibility = if (networkState.status == Status.FAILED) View.VISIBLE else View.GONE
        loadingProgressBar.visibility = if (networkState.status == Status.RUNNING) View.VISIBLE else View.GONE
        globalNetworkState.visibility = if (networkState.status == Status.SUCCESS) View.GONE else View.VISIBLE
        //set default state
        mSwipeRefreshLayout.isEnabled = networkState.status == Status.SUCCESS

    }

    @Optional
    @OnClick(R.id.retryLoadingButton)
    internal fun retryInitialLoading() {
        shotsViewModel.retry()
    }

    override fun retry() {
        shotsViewModel.retry()
    }

    override fun onShotClicked(position: Int) {
        val shotItem = shotListAdapter.getShotClicked(position)
        shotsViewModel.onShotClicked(shotItem!!, position)
    }


}
