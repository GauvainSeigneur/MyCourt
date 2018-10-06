package seigneur.gauvain.mycourt.ui.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.ui.shotDraft.ShotDraftFragment;
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity;
import seigneur.gauvain.mycourt.ui.shots.ShotsFragment;
import seigneur.gauvain.mycourt.ui.user.UserFragment;
import seigneur.gauvain.mycourt.utils.FragmentStateManager;

public class MainActivity extends BaseActivity implements HasSupportFragmentInjector {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private MainViewModel mMainViewModel;

    @BindView(R.id.fragment_place_holder)
    FrameLayout mFragmentContainer;

    @BindView(R.id.navigation)
    BottomNavigationView mNavigation;

    @BindView(R.id.fab_add_shot)
    FloatingActionButton mFabAddShot;

    private boolean isFAbVisible;

    private FragmentStateManager mFragmentStateManager;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initFragmentManager(savedInstanceState);
        mNavigation.setOnNavigationItemSelectedListener(item -> {
            mMainViewModel.setmBottomNavPos(getNavPositionFromMenuItem(item));
            mMainViewModel.onBottomNavItemSelected();
            return true;
        });

        mNavigation.setOnNavigationItemReselectedListener(item -> {
            mMainViewModel.setmBottomNavPos(getNavPositionFromMenuItem(item));
            mMainViewModel.onBottomNavItemReselected();
        });

        mMainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        mMainViewModel.init();

    }

    private void subscribeToSingleEvents(MainViewModel viewModel) {
        // BOTTOM NAV EVENTS
        viewModel.getNavItemSelectedEvent().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer pos) {
                if (pos!=null && pos!=-1)
                    showFragment(pos);
            }
        });

        viewModel.getNavItemreselectedEvent().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer pos) {
                goBackOnTop(pos);
            }
        });

        viewModel.getbackNavSystemCommand().observe(
                this,
                pos -> {
                    goBackOnPrevItem(pos);
                }
        );

        viewModel.getFinishCommand().observe(this,
                call -> { finish(); }
                );

        viewModel.getEditCommand().observe(this,
                call -> { goToShotEdition(); }
        );
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeToSingleEvents(mMainViewModel);
        mMainViewModel.checkIfTokenIsNull();

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        mMainViewModel.onReturnNavigation();
    }

    @OnClick(R.id.fab_add_shot)
    public void goToEdition() {
        mMainViewModel.onAddFabclicked();
    }


    public void goBackAtStart(int position) {
        goBackOnTop(position);
    }


    public void closeActivity() {
        finish();
    }


    public void showFragment(int pos) {
        mFragmentStateManager.changeFragment(pos);
    }


    public void goBackOnPrevItem(int position) {
        mNavigation.setSelectedItemId(mNavigation.getMenu().getItem(position).getItemId());
    }


    public void goToShotEdition() {
        //todo - add some keys to intent - define from which  fragment user has clicked!
        Intent intent = new Intent(this, EditShotActivity.class);
        startActivity(intent);
    }


    public void showMessageShotPublished(){

    }


    public void showMessageShotDrafted() {

    }

    public void showNoInternetConnectionMessage(boolean showIt) {
        if(showIt) {
        }
    }

    public void showInternetConnectionRetrieved(boolean showIt) {
        if(showIt) {
        }

    }

    private void initFragmentManager(Bundle savedInstanceState) {
        showFAB(true);
        mFragmentStateManager = new FragmentStateManager(mFragmentContainer, getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch(position) {
                    case 0:
                        //return new ShotsFragment();
                        return new ShotsFragment();
                    case 1:
                        return new ShotDraftFragment();
                    case 2:
                        return new UserFragment();
                }
                //return new ShotsFragment();
                return new ShotsFragment();
            }
        };
        if (savedInstanceState == null) {
            mFragmentStateManager.changeFragment(0);
        }
    }

    int getNavPositionFromMenuItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
                showFAB(true);
                return 0;
            case R.id.navigation_dashboard:
                showFAB(true);
                return 1;
            case R.id.navigation_notifications:
                showFAB(false);
                return 2;
            default:
                return -1;
        }
    }

    public void goBackOnTop(int position) {
        Toast.makeText(this, "go back on top", Toast.LENGTH_SHORT).show();
    }

    public void showFAB(boolean show) {
        if (show && !isFAbVisible) {
            mFabAddShot.show();
            isFAbVisible=true;
        } else if (!show && isFAbVisible) {
            mFabAddShot.hide();
            isFAbVisible = false;
        }
    }

}
