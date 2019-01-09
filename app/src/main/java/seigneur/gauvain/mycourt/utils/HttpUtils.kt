package seigneur.gauvain.mycourt.utils

import android.content.Context
import android.net.Uri

import java.io.File

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import seigneur.gauvain.mycourt.utils.image.ImageUtils

class HttpUtils {
    companion object {

        /**
         * Create MultipartBody.Part instance separated in order to use @PartMap annotation
         * to pass parameters along with File request. PartMap is a Map of "Key" and RequestBody.
         * See : https://stackoverflow.com/a/40873297
         */
        fun createFilePart(context: Context,
                           fileUri: Uri?,
                           imageFormat: String?,
                           partName: String): MultipartBody.Part {
            //Get file
            val uriOfFile = ImageUtils.getRealPathFromImage(context, fileUri)
            val file = File(uriOfFile)
            val imageFormatFixed: String?
            //Word around for jpg format - refused by dribbble
            if (imageFormat == "jpg")
                imageFormatFixed = "JPG" // todo - to be tested
            else
                imageFormatFixed = imageFormat
            // create RequestBody instance from file
            val requestFile = RequestBody.create(MediaType.parse("image/$imageFormatFixed"), file)
            // MultipartBody.Part is used to send also the actual file name
            return MultipartBody.Part.createFormData(partName, file.name, requestFile)
        }
    }
}