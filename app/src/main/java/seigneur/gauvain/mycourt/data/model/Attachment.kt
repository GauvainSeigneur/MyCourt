package seigneur.gauvain.mycourt.data.model

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity
data class Attachment(
        @SerializedName("id")
        var id:Long,
        var shotId:String?="",
        @SerializedName("url")
        var uri: String,
        @SerializedName("content_type")
        var contentType: String?="",
        var fileName: String?="") {


}
