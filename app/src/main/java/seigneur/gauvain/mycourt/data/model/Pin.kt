package seigneur.gauvain.mycourt.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

//insert relation : https://stackoverflow.com/questions/44667160/android-room-insert-relation-entities-using-room
@Entity
data class Pin(@field:PrimaryKey(autoGenerate = true)
          var id: Int, var cryptedPIN: String, var initVector: String)
