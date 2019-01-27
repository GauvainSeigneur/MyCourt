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
import droidninja.filepicker.FilePickerConst
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Attachment
import seigneur.gauvain.mycourt.ui.shotEdition.ShotEditionViewModel
import seigneur.gauvain.mycourt.utils.FileUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.ArrayList
import seigneur.gauvain.mycourt.utils.Constants
import android.media.MediaMetadataRetriever




object ImagePicker {
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
        if (imageSize!=null)
            Timber.d("image size before cropping "+imageSize[0]+" "+imageSize[1])
        else
            Timber.d("image size empty")
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
                /* if (imageSize[0] >= 1600 && imageSize[1] >= 1200) {
                    options.withMaxResultSize(1600, 1200)
                } else if ( imageSize[0] <= 1600 && imageSize[1] <= 1200 && imageSize[0] >= 800 && imageSize[1] >= 600) {
                    options.withMaxResultSize(800, 600)
                } else {
                    options.withMaxResultSize(400, 300)
                }*/
                options.setCompressionQuality(100)
                options.setMaxBitmapSize(100000)
                UCrop.of(source, destination)
                        .withAspectRatio(4f, 3f)
                        .withOptions(options)
                        .start(activity)
            }
        }
    }

    fun onImagePicked (
             data: Intent?,
             shotEditionViewModel: ShotEditionViewModel){
        data?.let {
            val pickedFiles = ArrayList<String>()
            pickedFiles.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
            //first get fie type
            val fileType  =  FileUtils.getMimeType((pickedFiles[0]))
            Timber.d("file type: $fileType")
            when(fileType){
                Constants.GIF -> managePickedImage(pickedFiles, shotEditionViewModel)
                Constants.JPG->  managePickedImage(pickedFiles, shotEditionViewModel)
                Constants.PNG -> managePickedImage(pickedFiles, shotEditionViewModel)
                Constants.MP4 -> managePickedVideo(pickedFiles, shotEditionViewModel)
                else -> Timber.d("unauthorized") //todo notify viewmodel
            }
        }
    }

    private fun managePickedImage(
            pickedImg :ArrayList<String>,
            shotEditionViewModel: ShotEditionViewModel) {
        //get file uri  and send it to ViewModel
        shotEditionViewModel.mPickedFileUri = Uri.parse(pickedImg[0])
        //get file name with extension
        val shotFileName = FileUtils.getFileName(Uri.parse(pickedImg[0]))
        //get file name without extension
        val shotFileNameWithoutExtension = shotFileName.substringBefore(".",shotFileName)
        //send name, mymeType and dimens to viewModel
        shotEditionViewModel.mPickedFileName = shotFileNameWithoutExtension
        shotEditionViewModel.mPickedFileMymeType = FileUtils.getMimeType((pickedImg[0]))
        shotEditionViewModel.mPickedImageDimens = FileUtils.getImageFilePixelSize(Uri.parse(pickedImg[0]))
        //notify viewModel to call Ucrop command
         shotEditionViewModel.onImagePicked()
    }


    private fun managePickedVideo(
            pickedVideo :ArrayList<String>,
            shotEditionViewModel: ShotEditionViewModel) {
        //Check duration and aspect ratio
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(pickedVideo[0])
        val width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        val height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        val duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
        Timber.d("video width $width height: $height")
        Timber.d("video duration $duration")
        retriever.release()

        val authorizedRatio = (4/3).toLong()
        val videoRatio = (width/height).toLong()
        val authorizedDuration = 24000

        if (videoRatio ==authorizedRatio && duration<=authorizedDuration) {
             //get file uri  and send it to ViewModel
             shotEditionViewModel.mPickedFileUri = Uri.parse(pickedVideo[0])
             //get file name with extension
             val shotFileName = FileUtils.getFileName(Uri.parse(pickedVideo[0]))
             //get file name without extension
             val shotFileNameWithoutExtension = shotFileName.substringBefore(".",shotFileName)
             //send name, mymeType and dimens to viewModel
             shotEditionViewModel.mPickedFileName = shotFileNameWithoutExtension
             shotEditionViewModel.mPickedFileMymeType = FileUtils.getMimeType((pickedVideo[0]))
             //shotEditionViewModel.mPickedImageDimens = FileUtils.getImageFilePixelSize(Uri.parse(pickedImg[0])) //todo - necessary for VIDEO?
             //notify viewModel tha the video is ready
             shotEditionViewModel.onFileReady(Uri.parse(pickedVideo[0]))
         } else {
             //todo view model error
            Timber.d("video not fit")
         }

    }

    fun onImageCropped (
            data: Intent?,
            shotEditionViewModel : ShotEditionViewModel){
        data?.let {
            shotEditionViewModel.onFileReady(UCrop.getOutput(data))
            shotEditionViewModel.mCroppedImgDimen =  FileUtils.getImageFilePixelSize(UCrop.getOutput(data)!!)
        }
    }

    fun onAttachmentPicked(data: Intent?, shotEditionViewModel : ShotEditionViewModel) {
        data?.let {
            val attachmentPaths = ArrayList<String>()
            attachmentPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
            if (attachmentPaths.isNotEmpty()) {
                Timber.d("SELECTED MEDIA" + attachmentPaths[0])
                val fileName = FileUtils.getFileName(Uri.parse(attachmentPaths[0]))
                val fileNameWithoutExtension = fileName.substringBefore(".", fileName)
                val attachment = Attachment(-1L,    //id to be -1 for new attachment
                        "",
                        attachmentPaths[0],
                        FileUtils.getMimeType((attachmentPaths[0])),
                        fileNameWithoutExtension)
                shotEditionViewModel.onAttachmentAdded(attachment)
            }
        }

    }



    /*
    ******************************************************************************************
    * OLD
    ******************************************************************************************/
    private const val TAG = "ImagePicker"
    private const val TEMP_IMAGE_NAME = "tempImage"

    //https://stackoverflow.com/questions/38619563/sending-image-file-with-retrofit-2

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

    private fun getTempFile(context: Context): File {
        val imageFile = File(context.externalCacheDir, TEMP_IMAGE_NAME)
        imageFile.parentFile.mkdirs()
        return imageFile
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
