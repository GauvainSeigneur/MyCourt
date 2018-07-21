package seigneur.gauvain.mycourt.ui.shotDraft.view;

import java.util.List;

import seigneur.gauvain.mycourt.data.model.ShotDraft;

public interface ShotDraftView {

    void stopRefresh();

    void showDraftList(List<ShotDraft> ShotDraft, boolean isRefreshing);

    void showEmptyView(boolean isVisible);

    void goToShotEdition();

}
