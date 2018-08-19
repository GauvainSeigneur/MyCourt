package seigneur.gauvain.mycourt.ui.main.presenter;

import android.view.Menu;
import android.view.MenuItem;

import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface MainPresenter extends BasePresenter {

    /**
     * User has clicked on item
     * @param item - item in the menu
     */
    void onBottomNavItemSelected(MenuItem item);

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
}
