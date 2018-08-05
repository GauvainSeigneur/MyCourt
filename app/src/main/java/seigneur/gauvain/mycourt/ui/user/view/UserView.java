package seigneur.gauvain.mycourt.ui.user.view;

import seigneur.gauvain.mycourt.data.model.User;

public interface UserView {

   void showNoConnectionView(boolean visible);

   void showNoUserFoundView(boolean visible);

   void setUpUserAccountInfo(User user);

    void setUserPicture(User user);

    void showNoTeamsView(boolean visible);

    void showUserLinks(User user);

    void showNoLinksView(boolean visible);

}
