package seigneur.gauvain.mycourt.utils.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import timber.log.Timber

import java.io.File
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import seigneur.gauvain.mycourt.R
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImagePicker {
    //https://stackoverflow.com/questions/38619563/sending-image-file-with-retrofit-2

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
     * get the file name of the picked Image - it allows to get the URI of the file image
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

    fun getImageFilePathFromContentUri(context: Context,fileName:String):Uri {
        //get Image picked file uri path from the one picked with picker
       // return Uri.fromFile(File(context.externalCacheDir, fileName))
        return Uri.fromFile(File(context.cacheDir, fileName))
    }

    private fun getTempFile(context: Context): File {
        val imageFile = File(context.externalCacheDir, TEMP_IMAGE_NAME)
        imageFile.parentFile.mkdirs()
        return imageFile
    }

    /**
     * Go to cropping activity
     * @param ImageCroppedFormat -  PNG, JPEG, GIF
     * @param source of the view  - set by the return intent from ImagePicker
     * @param destination - uri of destination folder: cacheDif
     * @param activity - context
     * @param imageSize - height and width in pixels
     */
    fun goToUCropActivity(ImageCroppedFormat: String?,
                          source: Uri,
                          destination: Uri,
                          activity: Activity,
                          imageSize: IntArray) {
        val options = UCrop.Options()
        options.setStatusBarColor(activity.resources.getColor(R.color.colorPrimaryDark))
        options.setToolbarColor(activity.resources.getColor(R.color.colorPrimary))
        options.setActiveWidgetColor(activity.resources.getColor(R.color.colorAccent))
        options.withMaxResultSize(1600, 1200)
        //if image is Gif, it can't be cropped. so check if the px size is in accordance to
        //Must be a 4:3 ratio between 400×300 and 1600×1200
        if (ImageCroppedFormat != null) {
            if (ImageCroppedFormat.contains( "gif")){
                //check if image aspect ratio is 4/3
                val gifRatio = imageSize[1].toDouble() / imageSize[0].toDouble()
                Timber.d("4/3 :$gifRatio")
                if (gifRatio == 0.75 && imageSize[0] <= 1600 && imageSize[1] <= 1200 && imageSize[0] > 400 && imageSize[1] >= 300) {
                    options.setHideBottomControls(true)
                    options.setAllowedGestures(UCropActivity.NONE, UCropActivity.NONE, UCropActivity.NONE)
                    UCrop.of(source, destination)
                            .useSourceImageAspectRatio() //use source aspect ratio
                            .withOptions(options)
                            .start(activity)
                    Toast.makeText(activity, "gif can't be cropped", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "gif can't be cropped and the format needs to be 4/3 (eg. 400px*300px)", Toast.LENGTH_SHORT).show()
                }
            } else {
                UCrop.of(source, destination)
                        .withAspectRatio(4f, 3f)
                        .withOptions(options)
                        .withMaxResultSize(1600, 1200)
                        .start(activity)
            }
        }
    }

    /*
    ******************************************************************************************
    * OLD
    ******************************************************************************************/
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
        //getImageSize(context,selectedImage!!)
        return selectedImage
    }

    private fun calculateFileSize(file: File): String {
        //String filepathstr=filepath.toString();
        // Get length of file in bytes
        val fileSizeInBytes:Long = file.length()
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        val fileSizeInKB = fileSizeInBytes / 1024
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        val fileSizeInMB = fileSizeInKB / 1024
        return (fileSizeInMB).toString()
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
