package seigneur.gauvain.mycourt.ui.user.view;

import seigneur.gauvain.mycourt.data.model.User;

public interface UserView {

    /**
     * Show a view when app lost internet connection
     * @param visible - show or hide it
     */
    void showNoConnectionView(boolean visible);

    /**
     * Show a view if no user was found
     * @param visible - show or hide it
     */
   void showNoUserFoundView(boolean visible);

    /**
     * Set up UI with user info fetched
     * @param user - User fetched
     */
   void setUpUserAccountInfo(User user);

    /**
     * Set up user picture
     * @param user - User fetched
     */
   void setUserPicture(User user);

    /**
     * Set up a view if user doesn't have team
     * @param visible - show or hide it
     */
   void showNoTeamsView(boolean visible);

    /**
     * show user links
     * @param user - User fetched
     */
   void showUserLinks(User user);

    /**
     * Set up a view if user doesn't have links
     * @param visible - show or hide it
     */
   void showNoLinksView(boolean visible);

}
