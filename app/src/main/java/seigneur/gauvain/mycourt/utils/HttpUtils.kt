package seigneur.gauvain.mycourt.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import com.yalantis.ucrop.UCrop

import java.io.File

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

class HttpUtils {
    companion object {

        /**
         * Create MultipartBody.Part instance separated in order to use @PartMap annotation
         * to pass parameters along with File request. PartMap is a Map of "Key" and RequestBody.
         * See : https://stackoverflow.com/a/40873297
         */
        fun createFilePart(context: Context,
                           originalImgDimen: IntArray,
                           fileUri: Uri?,
                           imageFormat: String?,
                           partName: String): MultipartBody.Part {
            //Get file
            //create a file to write bitmap data
            val f =  createCacheFileFromBitmap(context, originalImgDimen, fileUri)
            Timber.d("originalImgDimen "+ originalImgDimen[0] + originalImgDimen[1])
            // create RequestBody instance from file
            val requestFile = RequestBody.create(MediaType.parse(imageFormat!!), f)
            Timber.d("requestFile $requestFile")
            // MultipartBody.Part is used to send also the actual file name
            return MultipartBody.Part.createFormData(partName, f.name, requestFile)


        }

        /**
         * As the Ucrop image resize function doesn't work well, we :
         * 1 - create bitmap from the Uri
         * 2 - adjust its width and height while keeping the ratio from cropping
         * 3 - convert it in a file to operation is finished
         */
        private fun createCacheFileFromBitmap(context: Context,
                                              originalImgDimen: IntArray,
                                              fileUri: Uri?): File {
            //Get file
            //create a file to write bitmap data
            val f =  File(context.cacheDir, fileUri?.lastPathSegment) //todo - change image name
            f.createNewFile()
            Timber.d("originalImgDimen "+ originalImgDimen[0] + originalImgDimen[1])
            //Convert bitmap to byte array
            val bitmap = FileUtils.getAdjustedBitmap(
                    originalImgDimen,
                    FileUtils.getFilePathFromContentUri(context, fileUri)!!)
            Timber.d("bitmap adjusted "+ bitmap.width)
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 /*ignored for PNG*/, bos)
            val bitmapdata = bos.toByteArray()
            //write the bytes in file
            val fos =  FileOutputStream(f)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            //recycler it after you don't need it
            bitmap.recycle()
            return f
        }

        //old version
        private fun createFileFromuri(context: Context, fileUri: Uri?):File {
            val uriOfFile = FileUtils.getFilePathFromContentUri(context, fileUri)
            Timber.d("uriOfFile $uriOfFile")
            return File(uriOfFile.toString())
        }
    }

}