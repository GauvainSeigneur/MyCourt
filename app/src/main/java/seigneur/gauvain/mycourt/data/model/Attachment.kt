package seigneur.gauvain.mycourt.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Attachment(
        var uri: Uri,
        var imageFormat: String)
