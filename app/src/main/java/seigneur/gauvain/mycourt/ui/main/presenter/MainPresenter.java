package seigneur.gauvain.mycourt.ui.main.presenter;

import android.view.MenuItem;

import seigneur.gauvain.mycourt.ui.base.mvp.BaseMVPView;
import seigneur.gauvain.mycourt.ui.base.mvp.BasePresenter;

public interface MainPresenter<V extends BaseMVPView> extends BasePresenter<V> {

    /**
     * User has clicked on item
     * @param pos - item in the menu
     */
    void onBottomNavItemSelected(int pos);

    /**
     * User has clicked on item already selected
     * @param position - item position in the menu
     */
    void onBottomNavItemReselected(int position);

    /**
     * User has clicked on the fab dedicated to create new Shot
     */
    void onAddFabclicked();

    /**
     * Callback when user has drafted an edition of a shot
     */
    void onReturnShotDrafted();

    /**
     * Callback when user has published an edition of a shot
     */
    void onReturnShotPublished();

    /**
     * Check internet connection sometimes
     */
    void onCheckInternetConnection();

    /**
     *  Sometimes, after a long pause, the data are deleted by Android, so we cannot perform
     *  any api operation. so check if user is null on resume and get it again from DB
     */
    void checkIfTokenIsNull();

    /**
     * User clicked on back button, go back on prev fragment
     */
    void onReturnNavigation(MenuItem item, int position);
}
