package seigneur.gauvain.mycourt.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import seigneur.gauvain.mycourt.R
import java.io.File
import android.webkit.MimeTypeMap
import android.graphics.BitmapFactory





object FileUtils {

    /**
     * get Android content uri (starting with Content//...) from Storage/XXX
     * path for a file
     */
    fun getContentUriFromFilePath(context: Context, contentUri: String?): Uri? {
        return if (contentUri!= null) {
            FileProvider.getUriForFile(
                    context,
                    context.getString(R.string.file_provider_authorities),
                    File(contentUri))
        } else {
            null
        }

    }

    fun getFileName(uri:Uri):String {
        return uri.lastPathSegment
    }

    //get mime type of file from file path (not from content uri)
    fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun getImageFilePixelSize(uri: Uri):IntArray {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path!!).absolutePath, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        return intArrayOf(imageWidth, imageHeight)
    }
}