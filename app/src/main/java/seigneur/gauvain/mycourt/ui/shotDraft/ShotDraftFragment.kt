package seigneur.gauvain.mycourt.ui.shotDraft

import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.widget.Toast
import java.util.ArrayList
import javax.inject.Inject
import butterknife.BindView
import com.squareup.haha.perflib.Main
import dagger.android.support.AndroidSupportInjection
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.ui.base.BaseFragment
import seigneur.gauvain.mycourt.ui.main.MainActivity
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity
import seigneur.gauvain.mycourt.utils.Constants
import timber.log.Timber


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

    @BindView(R.id.rv_shot_draft)
    lateinit var shotDraftRV: RecyclerView

    private var mShotDraftsListAdapter: ShotDraftsListAdapter?=null
    private var shotDraftsSaved: MutableList<Draft> = ArrayList()

    override val fragmentLayout: Int
        get() = R.layout.fragment_shot_draft

    /*
    ************************************************************************************
    *  Fragment lifecycle
    ************************************************************************************/
    override fun onAttach(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this)
        }
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        //get list
        mShotDraftViewModel.fetchShotDrafts()
        //Override anonymous interface function
        val listCallback = object : ShotDraftListCallback {
            override fun onShotDraftClicked(shotDraft: Draft, position: Int) {
                if ((activity as MainActivity).isEditModeActivated!!)
                    manageListSelection(position)
                else
                    mShotDraftViewModel.onShotDraftClicked(shotDraft, position)
            }

            override fun onShotDraftLongClicked(position: Int) {
                mShotDraftsListAdapter?.addIDIntoSelectedIds(position)
                if (mShotDraftsListAdapter?.selectedIds!!.isNotEmpty()) {
                    (activity as MainActivity).mCustomBottomActionmode.startMode()
                } else {
                    (activity as MainActivity).mCustomBottomActionmode.stopMode()
                }
            }
        }

        mShotDraftsListAdapter = ShotDraftsListAdapter(context!!,
                shotDraftsSaved,
                listCallback,
                activity as MainActivity)
    }

    override fun onCreateView(inRootView: View, inSavedInstanceState: Bundle?) {
        Timber.d("onCreateView")
        toolbar.inflateMenu(R.menu.menu_shot_detail)
        toolbar.setOnMenuItemClickListener(this)
        shotDraftRV.layoutManager = GridLayoutManager(context, 2)
        shotDraftRV.adapter = mShotDraftsListAdapter
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //listen liveData
        subscribeToSingleEvents(mShotDraftViewModel)
        subscribeLiveData(mShotDraftViewModel)
    }
    /*
    ************************************************************************************
    *  OnMenuItemClickListener
    ***********************************************************************************/
    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_settings -> {
                Toast.makeText(mApplication, "yeah!", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

    /*
    ************************************************************************************
    *  Private functions
    ************************************************************************************/
    private fun subscribeToSingleEvents(viewModel: ShotDraftViewModel) {
        viewModel.dbChanged().observe(
                this,
                Observer {
                    _ -> mShotDraftViewModel.fetchShotDrafts()
                }
        )

        viewModel.itemClickedEvent.observe(
                this,
                Observer<Draft> { _ ->
                    val intent = Intent(activity, EditShotActivity::class.java)
                    startActivity(intent)
                }
        )

        viewModel.deleteCickEvent().observe(
                this,
                Observer { _ -> deleteItems()
                }
        )

        viewModel.getDeleteOpeResult().observe(
                this,
                Observer { result ->
                    if (result==0) //success
                        (activity as MainActivity).mCustomBottomActionmode.stopMode()
                    else
                        Timber.d("MAKE SOMETHING !!!")
                }
        )
    }

    private fun  subscribeLiveData(shotDraftViewModel: ShotDraftViewModel) {
        shotDraftViewModel.drafts
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

        shotDraftViewModel.editMode
                .observe(
                        this,
                        Observer { mode ->
                            if (mode==Constants.CUSTOM_ACTION_MODE_ON) {
                                Timber.d("activated ")
                            } else {
                                Timber.d("DEactivated ")
                                if (mShotDraftsListAdapter?.selectedIds!!.isNotEmpty()) {
                                    mShotDraftsListAdapter?.selectedIds!!.clear()
                                    mShotDraftsListAdapter?.notifyDataSetChanged()
                                }
                            }
                        }
                )
    }

    private fun showDraftList(shotDraft: List<Draft>?, isRefreshing: Boolean) {
       mShotDraftsListAdapter!!.clear() //TODO - MANAGE THIS EVENT MORE PROPERLY
       shotDraftsSaved.addAll(shotDraft!!)
       mShotDraftsListAdapter!!.notifyDataSetChanged()
    }

    private fun deleteItems() {
        //todo - delete only in recyclerview and display an snkacbar to cancel or
        // if is not canceled , delete from DB
        //1 - deleteSelectedIds
        //2 - display snackbar
        //3 - mShotDraftViewModel.deleteSelectDrafts(ArrayList(mShotDraftsListAdapter!!.selectedIds))
        mShotDraftViewModel.deleteSelectDrafts(ArrayList(mShotDraftsListAdapter!!.selectedIds))
    }

    private fun showEmptyView(isVisible: Boolean) {
        if (isVisible)
            Toast.makeText(mApplication, "empty view", Toast.LENGTH_SHORT).show()
    }

    private fun goToShotEdition() {
        val intent = Intent(activity, EditShotActivity::class.java)
        startActivity(intent)
    }

    private fun manageListSelection(position: Int) {
        //TODO - USE LIVEDATA
        mShotDraftsListAdapter?.addIDIntoSelectedIds(position)
        //disable action if list is empty after deselect an item
        if (mShotDraftsListAdapter?.selectedIds!!.isEmpty())
            (activity as MainActivity).mCustomBottomActionmode.stopMode()
    }

}
