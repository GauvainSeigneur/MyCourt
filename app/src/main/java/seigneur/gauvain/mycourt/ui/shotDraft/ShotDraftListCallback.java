package seigneur.gauvain.mycourt.ui.shotDraft;

import seigneur.gauvain.mycourt.data.model.Draft;

/**
 * UI callback for ShotDraft List
 */
public interface ShotDraftListCallback {

    void onShotDraftClicked(Draft shotDraft, int position);

}
