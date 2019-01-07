package seigneur.gauvain.mycourt.utils.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import timber.log.Timber

import java.io.File
import java.lang.Long
import android.widget.Toast
import android.graphics.Bitmap
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.IOException


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
        getImageSize(context,selectedImage!!)
        return selectedImage
    }

    private fun calculateFileSize(file: File): String {
        //String filepathstr=filepath.toString();
        // Get length of file in bytes
        val fileSizeInBytes = file.length()
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        val fileSizeInKB = fileSizeInBytes / 1024
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        val fileSizeInMB = fileSizeInKB / 1024
        Timber.d("imagesize: "+ Long.toString(fileSizeInBytes))
        return Long.toString(fileSizeInMB)
    }

    private fun getTempFile(context: Context): File {
        val imageFile = File(context.externalCacheDir, TEMP_IMAGE_NAME)
        imageFile.parentFile.mkdirs()
        return imageFile
    }

    @Throws(IOException::class)
    private fun getImageSize(context: Context, choosen: Uri) {
        val bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), choosen)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageInByte = stream.toByteArray()
        val lengthbmp = imageInByte.size.toLong()
        // Get length of file in bytes
        val fileSizeInBytes = imageInByte.size.toLong()
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        val fileSizeInKB = (fileSizeInBytes / 1024).toLong()
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        val fileSizeInMB = (fileSizeInKB / 1024).toLong()
        Timber.d("image size:"+fileSizeInBytes)

    }

}
