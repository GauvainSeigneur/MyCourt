package seigneur.gauvain.mycourt.data.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

import com.google.gson.annotations.SerializedName

import java.util.ArrayList
import java.util.Date

//minimum constructor
data class Shot (
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String?,
        var title: String?,
        var description: String?,
        @SerializedName("tags")
        var tagList: ArrayList<String>?,
        @SerializedName("attachments")
        var attachment: List<Attachment>?) {

    //exclude properties from the generated implementations, declare it inside the class body:
    var html_url: String? =""
    var width: Int? =0
    var height: Int? =0
    var images: Map<String, String>? =null
    var animated: Boolean? = false
    var isLow_profile: Boolean? = false
    @SerializedName("published_at")
    var publishDate: Date? = null
    @SerializedName("updated_at")
    var updateDate: Date? = null

    val imageUrl: String
        get() {
            if (images == null) {
                return ""
            }
            val url = if (images!!.containsKey(IMAGE_HIDPI) && images!![IMAGE_HIDPI] != null)
                images!![IMAGE_HIDPI]
            else
                images!![IMAGE_NORMAL]
            return url ?: ""
        }

    val imageHidpi: String
        get() {
            if (images == null) {
                return ""
            }
            val url = if (images!!.containsKey(IMAGE_HIDPI) && images!![IMAGE_HIDPI] != null)
                images!![IMAGE_HIDPI]
            else
                images!![IMAGE_NORMAL]
            return url ?: ""
        }

    val imageNormal: String
        get() {
            if (images == null) {
                return ""
            }

            val imageUrlNormal = images!![IMAGE_NORMAL]
            return imageUrlNormal ?: ""
        }

    val imageTeaser: String
        get() {
            if (images == null) {
                return ""
            }

            val url = images!![IMAGE_TEASER]
            return url ?: ""
        }

    companion object {
        //Dribbble API
        val IMAGE_NORMAL = "normal"
        val IMAGE_HIDPI = "hidpi"
        val IMAGE_TEASER = "teaser"
    }

}