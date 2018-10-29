package seigneur.gauvain.mycourt.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import com.google.gson.annotations.SerializedName

import java.util.Date

@Entity
data class Token(
    @PrimaryKey
    var id: Int? = 0,
    @SerializedName("access_token")
    var accessToken: String,
    @SerializedName("token_type")
    var tokenType: String,
    @SerializedName("scope")
    var tokenScope: String
)
