package seigneur.gauvain.mycourt.ui.shotDraft

import seigneur.gauvain.mycourt.data.model.Draft

/**
 * UI callback for ShotDraft List
 */
interface ShotDraftListCallback {

    fun onShotDraftClicked(shotDraft: Draft, position: Int)

}
