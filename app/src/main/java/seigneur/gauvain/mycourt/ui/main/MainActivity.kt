package seigneur.gauvain.mycourt.ui.main

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.about.AboutFragment
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftFragment
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity
import seigneur.gauvain.mycourt.ui.shots.ShotsFragment
import seigneur.gauvain.mycourt.ui.user.UserFragment
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.FragmentStateManager
import timber.log.Timber

class MainActivity : BaseActivity(), HasSupportFragmentInjector, CustomBottomActionMode.DraftListEditMode {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mMainViewModel: MainViewModel by lazy {
         ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    @BindView(R.id.fragment_place_holder)
    lateinit var mFragmentContainer: FrameLayout

    @BindView(R.id.bottom_app_bar)
    lateinit var mBottomAppBar: BottomAppBar

    @BindView(R.id.navigation)
    lateinit var mNavigation: BottomNavigationView

    @BindView(R.id.fab_add_shot)
    lateinit var mFabAddShot: FloatingActionButton

    private var mFragmentStateManager: FragmentStateManager? = null

    var isEditModeActivated:Boolean?=false
    val mCustomBottomActionmode :CustomBottomActionMode by lazy {
        CustomBottomActionMode(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        initFragmentManager(savedInstanceState)
        mMainViewModel.init()
        mNavigation.setOnNavigationItemSelectedListener { item ->
            mMainViewModel.setmBottomNavPos(getNavPositionFromMenuItem(item))
            mMainViewModel.onBottomNavItemSelected()
            true
        }

        mNavigation.setOnNavigationItemReselectedListener { item ->
            mMainViewModel.setmBottomNavPos(getNavPositionFromMenuItem(item))
            mMainViewModel.onBottomNavItemReselected()
        }

    }

    @Optional
    @OnClick(R.id.fab_add_shot)
    fun goToEdition() {
        if (isEditModeActivated!!)
            mMainViewModel.onDeleteClicked()
        else
            mMainViewModel.onAddFabclicked()
    }

    /*
    ************************************************************************************
    *   CustomBottomActionMode.DraftListEditMode
    ************************************************************************************/
    override fun onListEditStart() {
        mMainViewModel.changeEditMode(Constants.CUSTOM_ACTION_MODE_ON)
    }

    override fun onListEditStop() {
        mMainViewModel.changeEditMode(Constants.CUSTOM_ACTION_MODE_OFF)
    }

    /*
    ************************************************************************************
    *   PRIVATE FUNCTIONS
    ************************************************************************************/
    private fun subscribeToLiveData(viewModel: MainViewModel) {
        viewModel.actionModeEvent.observe(this,
                Observer { mode ->
                    if (mode==Constants.CUSTOM_ACTION_MODE_ON) {
                        setUpCustomActionMode(true)
                    } else {
                        setUpCustomActionMode(false)
                    }

                  })

    }

    private fun subscribeToSingleEvents(viewModel: MainViewModel) {
        // BOTTOM NAV EVENTS
        viewModel.navItemSelectedEvent.observe(this,
                Observer { pos ->
                    if (pos != null && pos != -1)
                        showFragment(pos)
        })

        viewModel.navItemreselectedEvent.observe(this, Observer { pos -> goBackOnTop(pos!!) })

        viewModel.getbackNavSystemCommand().observe(
                this,
                Observer<Int> {
                    pos -> goBackOnPrevItem(pos!!)
                }
        )

        viewModel.finishCommand.observe(this,
                Observer<Void> {
                    _ -> finish()
                }
        )

        viewModel.editCommand.observe(this,
                Observer<Void> {
                    _ -> goToShotEdition()
                }
        )
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment>? {
        return fragmentDispatchingAndroidInjector
    }

    public override fun onResume() {
        super.onResume()
        subscribeToSingleEvents(mMainViewModel)
        subscribeToLiveData(mMainViewModel)
        mMainViewModel.checkIfTokenIsNull()

    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (isEditModeActivated!!)
            mCustomBottomActionmode.stopMode()
        else
            mMainViewModel.onReturnNavigation()
    }

    fun goBackAtStart(position: Int) {
        goBackOnTop(position)
    }


    fun closeActivity() {
        finish()
    }


    private fun showFragment(pos: Int) {
        mFragmentStateManager?.changeFragment(pos)
    }


    private fun goBackOnPrevItem(position: Int) {
        mNavigation.selectedItemId = mNavigation.menu.getItem(position).itemId
    }


    private fun goToShotEdition() {
        val intent = Intent(this, EditShotActivity::class.java)
        startActivity(intent)
    }

    private fun initFragmentManager(savedInstanceState: Bundle?) {
        mFragmentStateManager = object : FragmentStateManager(mFragmentContainer, supportFragmentManager) {
            override fun getItem(position: Int): androidx.fragment.app.Fragment {
                when (position) {
                    0 -> return ShotsFragment()
                    1 -> return ShotDraftFragment()
                    3 -> return UserFragment()
                    4 -> return AboutFragment()
                }
                return ShotsFragment()
            }
        }
        if (savedInstanceState == null) {
            mFragmentStateManager!!.changeFragment(0)
        }
    }

    private fun getNavPositionFromMenuItem(menuItem: MenuItem): Int {
        when (menuItem.itemId) {
            R.id.navigation_home                -> return 0
            R.id.navigation_dashboard           -> return 1
            R.id.blank_nav_item                 -> return 2
            R.id.navigation_notifications       -> return 3
            R.id.navigation_about               -> return 4
            else                                -> return -1
        }
    }

    private fun goBackOnTop(position: Int) {
        Toast.makeText(this, "go back on top", Toast.LENGTH_SHORT).show()
    }

    private fun setUpCustomActionMode(isActivated:Boolean?=false) {
        isEditModeActivated=isActivated
        if (isActivated==true) {
            Timber.d("Edit Mode activated")
            mNavigation.visibility= View.INVISIBLE
            // Move FAB from the center of BottomAppBar to the end of it
            mBottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
            // Change FAB icon
            mFabAddShot.setImageResource(R.drawable.ic_delete_black_24dp)
        } else {
            Timber.d("Edit Mode deactivated")
            mNavigation.visibility= View.VISIBLE
            // Move FAB from the center of BottomAppBar to the center of it
            mBottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
            // Change FAB icon
            mFabAddShot.setImageResource(R.drawable.ic_add_black_24dp)
        }
    }


}
