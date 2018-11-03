package seigneur.gauvain.mycourt.ui.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftFragment
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity
import seigneur.gauvain.mycourt.ui.shots.ShotsFragment
import seigneur.gauvain.mycourt.ui.user.UserFragment
import seigneur.gauvain.mycourt.utils.FragmentStateManager

class MainActivity : BaseActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mMainViewModel: MainViewModel by lazy {
         ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    @BindView(R.id.fragment_place_holder)
    lateinit var mFragmentContainer: FrameLayout

    @BindView(R.id.navigation)
    lateinit var mNavigation: BottomNavigationView

    @BindView(R.id.fab_add_shot)
    lateinit var mFabAddShot: FloatingActionButton

    private val isFAbVisible: Boolean = false

    private var mFragmentStateManager: FragmentStateManager? = null

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

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

    private fun subscribeToSingleEvents(viewModel: MainViewModel) {
        // BOTTOM NAV EVENTS
        viewModel.navItemSelectedEvent.observe(this, Observer { pos ->
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
        mMainViewModel.checkIfTokenIsNull()

    }

    override fun onBackPressed() {
        //super.onBackPressed();
        mMainViewModel.onReturnNavigation()
    }

    @OnClick(R.id.fab_add_shot)
    fun goToEdition() {
        mMainViewModel.onAddFabclicked()
    }


    fun goBackAtStart(position: Int) {
        goBackOnTop(position)
    }


    fun closeActivity() {
        finish()
    }


    fun showFragment(pos: Int) {
        mFragmentStateManager?.changeFragment(pos)
    }


    fun goBackOnPrevItem(position: Int) {
        mNavigation.selectedItemId = mNavigation.menu.getItem(position).itemId
    }


    fun goToShotEdition() {
        //todo - add some keys to intent - define from which  fragment user has clicked!
        val intent = Intent(this, EditShotActivity::class.java)
        startActivity(intent)
    }


    fun showMessageShotPublished() {

    }


    fun showMessageShotDrafted() {

    }

    fun showNoInternetConnectionMessage(showIt: Boolean) {
        if (showIt) {
        }
    }

    fun showInternetConnectionRetrieved(showIt: Boolean) {
        if (showIt) {
        }

    }

    private fun initFragmentManager(savedInstanceState: Bundle?) {
        mFragmentStateManager = object : FragmentStateManager(mFragmentContainer, supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                when (position) {
                    0 -> return ShotsFragment()
                    1 -> return ShotDraftFragment()
                    3 -> return UserFragment()
                }
                return ShotsFragment()
            }
        }
        if (savedInstanceState == null) {
            mFragmentStateManager!!.changeFragment(0)
        }
    }

    internal fun getNavPositionFromMenuItem(menuItem: MenuItem): Int {
        when (menuItem.itemId) {
            R.id.navigation_home -> return 0
            R.id.navigation_dashboard -> return 1
            R.id.blank_nav_item -> return 2
            R.id.navigation_notifications -> return 3
            R.id.navigation_about -> return 4
            else -> return -1
        }
    }

    fun goBackOnTop(position: Int) {
        Toast.makeText(this, "go back on top", Toast.LENGTH_SHORT).show()
    }


}
