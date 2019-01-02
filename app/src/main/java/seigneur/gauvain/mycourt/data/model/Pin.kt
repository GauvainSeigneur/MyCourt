package seigneur.gauvain.mycourt.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

//insert relation : https://stackoverflow.com/questions/44667160/android-room-insert-relation-entities-using-room
@Entity
data class Pin(
        @field:PrimaryKey(autoGenerate = true)
        var id: Int,
        var cryptedPIN: String,
        var initVector: String)
