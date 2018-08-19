package seigneur.gauvain.mycourt.ui.shotDraft.view;

import seigneur.gauvain.mycourt.data.model.ShotDraft;

/**
 * UI callback for ShotDraft List
 */
public interface ShotDraftListCallback {

    void onShotDraftClicked(ShotDraft shotDraft,int position);

}
