package seigneur.gauvain.mycourt.ui.main.view;

import android.view.MenuItem;

public interface MainView {

    /**
     * go back at the top of the fragment
     * @param position - position of the item selected in bottom navigation
     */
    void goBackAtStart(int position);

    /**
     * Show dedicated fragment when user click on the dedicated item in the menu
     * @param pos -  item of bottom nav menu
     */
    void showFragment(int pos);

    /**
     * go Back on prev item when user click on back button
     * @param pos -  item of bottom nav menu
     */
    void goBackOnPrevItem(int pos);

    /**
     * Go to EditShotActivity
     */
    void goToShotEdition();

    /**
     * Show a confirmation message when shot has been published
     */
    void showMessageShotPublished();

    /**
     * Show a confirmation message when shot has been drafted
     */
    void showMessageShotDrafted();

    /**
     * Show a message when internet connection is lost
     */
    void showNoInternetConnectionMessage(boolean showIt);

    /**
     * Show a message when internet connection is retrieved after being lost
     */
    void showInternetConnectionRetrieved(boolean showIt);

    /**
     * Finish activity
     */
    void closeActivity();

}
