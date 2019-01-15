package seigneur.gauvain.mycourt.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import seigneur.gauvain.mycourt.R
import java.io.File
import android.webkit.MimeTypeMap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import timber.log.Timber
import android.R.attr.bitmap
import java.io.FileOutputStream
import java.io.IOException


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

    /**
     * Get the the file system path from the content provider uri
     * @param context - activity or fragment
     * @param selectedImageUri - Uri of the image
     * @return the system file url (get the file)
     */
    fun getFilePathFromContentUri(context: Context, selectedImageUri: Uri?): String? {
        var selectedImagePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(selectedImageUri!!,
                projection, null, null, null)
        if (cursor == null) {
            selectedImagePath = selectedImageUri.path
        } else {
            if (!cursor.moveToFirst()) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                selectedImagePath = cursor.getString(idx)
            }
        }
        cursor?.let{
            cursor.close()
        }
        return selectedImagePath
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

    fun getAdjustedBitmap(orginalImgDimen:IntArray, imagePath: String):Bitmap {
        val adjustedBitmap:Bitmap
        if (orginalImgDimen[0] >= 1600 && orginalImgDimen[1] >= 1200) {
             adjustedBitmap = getResizedBitmap(1600, 1200, imagePath)
         } else if (orginalImgDimen[0] >= 800 && orginalImgDimen[1] >= 600) {
             adjustedBitmap = getResizedBitmap(800, 600, imagePath)
         } else if (orginalImgDimen[0] >= 400 && orginalImgDimen[1] >= 300) {
            adjustedBitmap = getResizedBitmap(400, 300, imagePath)
         } else {
            adjustedBitmap = getResizedBitmap(400, 300, imagePath)
        }
        //do not forget to recycle it after you use it !
        return adjustedBitmap
    }

    private fun getResizedBitmap(targetW: Int,
                         targetH: Int,
                         imagePath: String): Bitmap {
        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        //inJustDecodeBounds = true <-- will not load the bitmap into memory
        bmOptions.inJustDecodeBounds = true
        //val originalBitmap:Bitmap = BitmapFactory.decodeFile(imagePath, bmOptions)
        val originalW = bmOptions.outWidth
        val originalH = bmOptions.outHeight
        Timber.d("original & h: "+originalW + " " +originalH )

        // load bitmap in memory to avoid null pointer exception
        bmOptions.inJustDecodeBounds = false
        bmOptions.inPurgeable = true
        val resizedBitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeFile(imagePath, bmOptions),
                targetW,
                targetH,
                true)
        return  resizedBitmap

    }


}