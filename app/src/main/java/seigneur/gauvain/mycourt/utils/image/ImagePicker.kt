package seigneur.gauvain.mycourt.utils.image

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log

import java.io.File


object ImagePicker {

    private const val TAG = "ImagePicker"
    private const val TEMP_IMAGE_NAME = "tempImage"

    fun pickImage(activity: Activity, requestCode: Int) {
        activity.startActivityForResult(
                createChooserIntent(),
                requestCode)
    }

    private fun createChooserIntent(): Intent {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return Intent.createChooser(intent, null)
    }


    /**
     * get the file name of the picked Image
     * @param context - context provided by activity
     * @param uri - uri from intent data
     * @return file name
     */
    fun getPickedImageName(context: Context, uri: Uri): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        val nameWithoutExtenstion: String
        if (name != null) {
            nameWithoutExtenstion = name.substring(0, name.lastIndexOf('.'))
        } else {
            val imageFile = getTempFile(context)
            nameWithoutExtenstion = imageFile.name
        }

        return nameWithoutExtenstion
    }

    fun getImageUriFromResult(context: Context, resultCode: Int, imageReturnedIntent: Intent?): Uri? {
        Log.d(TAG, "getImageFromResult, resultCode: $resultCode")
        val imageFile = getTempFile(context)
        var selectedImage: Uri? = null
        if (resultCode == Activity.RESULT_OK) {
            val isCamera = imageReturnedIntent == null ||
                    imageReturnedIntent.data == null ||
                    imageReturnedIntent.data!!.toString().contains(imageFile.toString())
            if (isCamera) {
                /** CAMERA  */
                selectedImage = Uri.fromFile(imageFile)
            } else {
                /** ALBUM  */
                selectedImage = imageReturnedIntent!!.data
            }
        }
        return selectedImage
    }

    private fun getTempFile(context: Context): File {
        val imageFile = File(context.externalCacheDir, TEMP_IMAGE_NAME)
        imageFile.parentFile.mkdirs()
        return imageFile
    }

}
