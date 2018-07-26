package seigneur.gauvain.mycourt.ui.shots.view;

import java.util.List;

import seigneur.gauvain.mycourt.data.model.Shot;

public interface ShotsView {

    void showFirstFecthErrorView(boolean isVisible);

    void showNextFetchError(boolean isVisible, String error);

    /**
     * User is set has a prospect, so he can upload shot,
     * but in this case he doesn't upload any shot
     * @param visible
     */
    void showEmptyListView(boolean visible);

    //void showRecyclerview(boolean visible);

    void addShots(List<Shot> shots); //must set as observable to ake loader visible again

    void clearShots();

    void showLoadingFooter(boolean visible);

    void showEndListMessage(boolean visible);

    void goToShotDetail(Shot shot, int position);

    void stopRefreshing();

}
