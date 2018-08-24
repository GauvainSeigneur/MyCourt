package seigneur.gauvain.mycourt.ui.shotDraft.view;

import java.util.List;

import seigneur.gauvain.mycourt.data.model.ShotDraft;

public interface ShotDraftView {

    /**
     * Stop refreshing UI
     */
    void stopRefresh();

    /**
     * Set Up list of draft visible to user
     * @param ShotDraft - list of shotDraft
     * @param isRefreshing - check if list is get from swipe refresh layout? If yes, stop it
     */
    void showDraftList(List<ShotDraft> ShotDraft, boolean isRefreshing);

    /**
     * Special view if the no draft was found in DB
     * @param isVisible - show or hide it
     */
    void showEmptyView(boolean isVisible);

    /**
     * Go to shot editShotActivity
     */
    void goToShotEdition();

}
