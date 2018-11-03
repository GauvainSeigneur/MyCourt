package seigneur.gauvain.mycourt.data.model

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Relation

import java.util.ArrayList
import java.util.Date

@Entity
data class Draft(
    @PrimaryKey(autoGenerate = true)
    var draftID: Long = 0, //Insert methods treat 0 as not-set while inserting the item, so Room will auto generated an ID
    var typeOfDraft: Int = Int.MIN_VALUE, //NEW SHOT OR UPDATE
    var imageUri: String? = "" ,//for viewing TODO - maybe delete ?
    var imageFormat: String? = "",
    var schedulingDate: Date?=null,//ONLY FOR UPDATE OF AN ALREADY PUBLISHED SHOT
    //Embed shot object
    @Embedded
    var shot: Shot
) {
    fun changeInfoFromEdit(
            inImageUri: String?,
            inImageFormat: String?,
            title: String?,
            desc: String?,
            tags: ArrayList<String>?) {
        imageUri = inImageUri
        imageFormat = inImageFormat
        shot.title = title
        shot.description = desc
        shot.tagList = tags
    }


}