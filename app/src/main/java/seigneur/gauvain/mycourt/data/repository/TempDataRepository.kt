package seigneur.gauvain.mycourt.data.repository

import javax.inject.Inject
import javax.inject.Singleton

import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot

/**
 * Class used to get reference of data which are not
 * saved in DB or SharedPrefs
 * eg. data send between presenters
 */
@Singleton
class TempDataRepository
//Used to notify that change has been made on Draft DB
//public boolean mDraftsChanged=false;

@Inject
constructor() {

    //Used to temporarily store Shot in order to send it between presenter...
    lateinit var shot: Shot
    //Used to temporarily store ShotDraft in order to send it between presenter...
    lateinit var shotDraft: Draft

    //Set to define the source of the edition to register the Draft in the right way
    var draftCallingSource: Int = 0

}
