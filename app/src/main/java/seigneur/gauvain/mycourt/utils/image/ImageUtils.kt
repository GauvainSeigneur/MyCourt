package seigneur.gauvain.mycourt.utils.image

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.widget.Toast

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

import seigneur.gauvain.mycourt.R
import timber.log.Timber

class ImageUtils {
    companion object {

        /**
         * Return file path of Image after being saved in gallery
         * Must use Single or Observable to get the Uri returned
         * @param ImageCroppedFormat -  PNG, JPEG, GIF
         * @param croppedFileUri -  uri of the image (in temp files) after being cropped
         * @param context - activity
         * @return the file absolute path saved
         * @throws Exception - stream broke up
         */
        @Throws(Exception::class)
        fun saveImageAndGetItsFinalUri(ImageCroppedFormat: String, croppedFileUri: Uri?, context: Context): String {
            Timber.d(croppedFileUri?.lastPathSegment)
            //1: define path and create folder "MyCourt" inside Gallery
            val appDirectoryName = "MyCourt"
            val myCourtDraftFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath,
                    appDirectoryName)
            myCourtDraftFolder.mkdirs()
            //2: give to image a name
            //String filename = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment())+"."+ImageCroppedFormat;
            val filename = croppedFileUri?.lastPathSegment + "."+ ImageCroppedFormat.substringAfterLast("/",ImageCroppedFormat )
            //3: create a file with the path of myCourtDraftFolder and the given name
            val saveFile = File(myCourtDraftFolder.absolutePath, filename)
            //4: Copy file and close process
            val inStream = FileInputStream(File(croppedFileUri?.path))
            val outStream = FileOutputStream(saveFile)
            val inChannel = inStream.channel
            val outChannel = outStream.channel
            inChannel.transferTo(0, inChannel.size(), outChannel)
            inStream.close()
            outStream.close()
            //5 : show to user some information
            Toast.makeText(context, R.string.notification_image_saved, Toast.LENGTH_SHORT).show()
            //showNotification(saveFile);
            //6: get the new URI of the file after it has been copied to save it in Room
            //Uri fileUri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authorities), saveFile);
            Timber.d("saved uri: "+saveFile.absolutePath)
            return saveFile.absolutePath//fileUri;
        }

        /**
         * get extension of Image from Image picker intent result
         * @param uri - uri the image selected by user
         * @return the format. must be JPG, PNG or GIF. Nothing else
         */
        fun getImageExtension(context: Context, uri: Uri): String? {
            val contentResolver = context.contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()
            // Return file Extension
            return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
        }


        /**
         * Transform image to bitmap
         * @param drawable - resource load from Glide
         * @return the bitmap
         */
        fun drawableToBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            // We ask for the bounds if they have been set as they would be most
            // correct, then we check we are  > 0
            val width = if (!drawable.bounds.isEmpty)
                drawable.bounds.width()
            else
                drawable.intrinsicWidth

            val height = if (!drawable.bounds.isEmpty)
                drawable.bounds.height()
            else
                drawable.intrinsicHeight

            // Now we check we are > 0
            val bitmap = Bitmap.createBitmap(if (width <= 0) 1 else width, if (height <= 0) 1 else height,
                    Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }

    }

}
