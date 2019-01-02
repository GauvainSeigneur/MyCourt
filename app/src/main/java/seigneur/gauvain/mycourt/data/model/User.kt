package seigneur.gauvain.mycourt.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.json.JSONArray

import java.util.ArrayList

//todo - for phase 3: manage teams
@Entity
data class User (
    @PrimaryKey
    @SerializedName("id")
    var id: Int,
    var name: String,
    var login: String,
    var html_url: String,
    var avatar_url: String,
    var bio: String,
    var location: String,
    var links: Map<String, String>,
    @SerializedName("can_upload_shot")
    var isAllowedToUpload: Boolean,
    @SerializedName("pro")
    var isPro: Boolean = false,
    @SerializedName("followers_count")
    var followers_count: Int,
    var type: String,
    var created_at: String
)

