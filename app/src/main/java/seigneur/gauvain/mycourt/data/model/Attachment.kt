package seigneur.gauvain.mycourt.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Attachment(
        var id:Long,
        var shotId:String,
        var uri: String,
        var imageFormat: String)
