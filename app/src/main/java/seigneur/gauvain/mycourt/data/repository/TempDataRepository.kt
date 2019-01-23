package seigneur.gauvain.mycourt.data.repository

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.utils.SingleLiveEvent

/**
 * Class used to get reference of data which are not
 * saved in DB or SharedPrefs
 * eg. data send between viewModel
 */
@Singleton
class TempDataRepository

@Inject
constructor() {

    //Used to temporarily store Shot in order to send it between viewModel
    lateinit var shot: Shot

    //Used to temporarily store ShotDraft in order to send it between viewModel
    lateinit var shotDraft: Draft

    //Set to define the source of the edition to register the Draft in the right way
    var draftCallingSource: Int = 0

    //Notify subscribers Tat action mode has been triggered or stopped
    var editMode = MutableLiveData<Int>()

    //delete selected item from draft fragment
    var deleteSelectedListCmd = SingleLiveEvent<Void>()

}
