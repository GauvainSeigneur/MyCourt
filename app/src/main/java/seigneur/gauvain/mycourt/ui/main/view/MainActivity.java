package seigneur.gauvain.mycourt.ui.main.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
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
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.ui.main.presenter.MainPresenter;
import seigneur.gauvain.mycourt.ui.shotDraft.view.ShotDraftFragment;
import seigneur.gauvain.mycourt.ui.shotEdition.view.EditShotActivity;
import seigneur.gauvain.mycourt.ui.shots.view.ShotsFragment;
import seigneur.gauvain.mycourt.ui.user.view.UserFragment;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.FragmentStateManager;

public class MainActivity extends BaseActivity implements MainView, HasSupportFragmentInjector {

    @Inject
    MainPresenter mMainPresenter;

    @BindView(R.id.fragment_place_holder)
    FrameLayout mFragmentContainer;

    @BindView(R.id.navigation)
    BottomNavigationView mNavigation;

    @BindView(R.id.fab_add_shot)
    FloatingActionButton mFabAddShot;

    private boolean isFAbVisible;

    private FragmentStateManager mFragmentStateManager;

    private int mBottomNavPosition=0;

    private MenuItem mItem;

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
            mItem=item;
            mBottomNavPosition = getNavPositionFromMenuItem(mItem);
            if (mBottomNavPosition != -1) {
                mMainPresenter.onBottomNavItemSelected(mBottomNavPosition);
                return true;
            }
            return false;
        });

        mNavigation.setOnNavigationItemReselectedListener(item -> {
            mItem=item;
            mBottomNavPosition = getNavPositionFromMenuItem(mItem);
            if (mBottomNavPosition != -1)
                mMainPresenter.onBottomNavItemReselected(mBottomNavPosition);
        });

    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMainPresenter.checkIfTokenIsNull();
        mMainPresenter.onCheckInternetConnection();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        mMainPresenter.onReturnNavigation(mItem, mBottomNavPosition);
    }

    @OnClick(R.id.fab_add_shot)
    public void goToEdition() {
        mMainPresenter.onAddFabclicked();
    }

    @Override
    public void goBackAtStart(int position) {
        goBackOnTop(position);
    }

    @Override
    public void closeActivity() {
        finish();
    }

    @Override
    public void showFragment(int pos) {
        mFragmentStateManager.changeFragment(pos);
    }

    @Override
    public void goBackOnPrevItem(int position) {
        mNavigation.setSelectedItemId(mNavigation.getMenu().getItem(position).getItemId());
    }

    @Override
    public void goToShotEdition() {
        //todo - add some keys to intent - define from which  fragment user has clicked!
        Intent intent = new Intent(this, EditShotActivity.class);
        startActivity(intent);
    }

    @Override
    public void showMessageShotPublished(){

    }

    @Override
    public void showMessageShotDrafted() {

    }


    @Override
    public void showNoInternetConnectionMessage(boolean showIt) {
        if(showIt) {
        }
    }

    @Override
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
                        return new ShotsFragment();
                    case 1:
                        return new ShotDraftFragment();
                    case 2:
                        return new UserFragment();
                }
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
