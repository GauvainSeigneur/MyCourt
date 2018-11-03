package seigneur.gauvain.mycourt.utils.image

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import android.util.Range
import android.webkit.MimeTypeMap
import android.widget.Toast

import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.util.Calendar

import seigneur.gauvain.mycourt.R
import timber.log.Timber

class ImageUtils {
    companion object {

        /**
         * get the uri of the saved cropped image: to be used to save ShotDraft object in Room
         * @return UriOfImageCroppedSaved
         */
        /**
         * set UriOgImageCropped
         * @param uriOfImageCroppedSaved
         */
        var uriOfImageCroppedSaved: Uri?=null;

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
            //if image is Gif, it can't be cropped. so check if the px size is in accordance to
            //Must be a 4:3 ratio between 400×300 and 1600×1200
            if (ImageCroppedFormat != null && ImageCroppedFormat == "gif") {
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
        fun saveImageAndGetItsFinalUri(ImageCroppedFormat: String, croppedFileUri: Uri, context: Context): String {
            Timber.d(croppedFileUri.lastPathSegment)
            //1: define path and create folder "MyCourt" inside Gallery
            val appDirectoryName = "MyCourt"
            val myCourtDraftFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath, appDirectoryName)
            myCourtDraftFolder.mkdirs()
            //2: give to image a name
            //String filename = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment())+"."+ImageCroppedFormat;
            val filename = croppedFileUri.lastPathSegment + "." + ImageCroppedFormat
            //3: create a file with the path of myCourtDraftFolder and the given name
            val saveFile = File(myCourtDraftFolder.absolutePath, filename)
            //4: Copy file and close process
            val inStream = FileInputStream(File(croppedFileUri.path!!))
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
         * Get the the file system path from the content provider uri
         * @param context - activity or fragment
         * @param selectedImageUri - Uri of the image
         * @return the system file url (get the file)
         */
        fun getRealPathFromImage(context: Context, selectedImageUri: Uri?): String? {
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
            return selectedImagePath
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


        /**
         * Get the image height and width to
         * @param context
         * @param uri
         * @param sampleSize
         * @return
         */
        fun imagePickedWidthHeight(context: Context, uri: Uri, sampleSize: Int): IntArray {
            val options = BitmapFactory.Options()
            var fileDescriptor: AssetFileDescriptor? = null
            var bitmap: Bitmap? = null
            try {
                fileDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r")
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor!!.fileDescriptor, null, options)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (bitmap != null) {
                Timber.d("image sized - width: " + bitmap.width + " height: " + bitmap.height)
                return intArrayOf(bitmap.width, bitmap.height)
            } else
                return intArrayOf(0, 0)

        }

        fun decodeBitmap(context: Context, uri: Uri, sampleSize: Int): Bitmap? {
            val options = BitmapFactory.Options()
            var fileDescriptor: AssetFileDescriptor? = null
            var actuallyUsableBitmap: Bitmap? = null
            try {
                fileDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r")
                actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor!!.fileDescriptor, null, options)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Timber.d("error")
            }

            return actuallyUsableBitmap
        }
    }

}

/*

public class ImageUtils {

    public static Uri mUriOfImageCroppedSaved;

    public ImageUtils(){}


public static void goToUCropActivity(String ImageCroppedFormat,
                                     Uri source,
                                     Uri destination,
                                     Activity activity,
                                     int[] imageSize) {
    UCrop.Options options = new UCrop.Options();
    options.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));
    options.setToolbarColor(activity.getResources().getColor(R.color.colorPrimary));
    options.setActiveWidgetColor(activity.getResources().getColor(R.color.colorAccent));
    //if image is Gif, it can't be cropped. so check if the px size is in accordance to
    //Must be a 4:3 ratio between 400×300 and 1600×1200
    if (ImageCroppedFormat!=null && ImageCroppedFormat.equals("gif")) {
        //check if image aspect ratio is 4/3
        double gifRatio=(double)imageSize[1]/(double)imageSize[0];
        Timber.d("4/3 :"+gifRatio);
        if (gifRatio==0.75 && imageSize[0]<=1600 && imageSize[1]<=1200 && imageSize[0]>400 && imageSize[1]>=300) {
            options.setHideBottomControls(true);
            options.setAllowedGestures(0,0,0);
            UCrop.of(source, destination)
                    .useSourceImageAspectRatio() //use source aspect ratio
                    .withOptions(options)
                    .start(activity);
            Toast.makeText(activity, "gif can't be cropped", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "gif can't be cropped and the format needs to be 4/3 (eg. 400px*300px)", Toast.LENGTH_SHORT).show();
        }
    } else {
        UCrop.of(source, destination)
                .withAspectRatio(4, 3)
                .withOptions(options)
                .withMaxResultSize(1600, 1200)
                .start(activity);
    }
}


    public static String saveImageAndGetItsFinalUri(String ImageCroppedFormat, Uri croppedFileUri, Context context) throws Exception {
        Timber.d(croppedFileUri.getLastPathSegment());
        //1: define path and create folder "MyCourt" inside Gallery
        String appDirectoryName = "MyCourt";
        File myCourtDraftFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), appDirectoryName);
        myCourtDraftFolder.mkdirs();
        //2: give to image a name
        //String filename = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment())+"."+ImageCroppedFormat;
        String filename = croppedFileUri.getLastPathSegment()+"."+ImageCroppedFormat;
        //3: create a file with the path of myCourtDraftFolder and the given name
        File saveFile = new File(myCourtDraftFolder.getAbsolutePath(), filename);
        //4: Copy file and close process
        FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
        FileOutputStream outStream = new FileOutputStream(saveFile);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
        //5 : show to user some information
        Toast.makeText(context, R.string.notification_image_saved, Toast.LENGTH_SHORT).show();
        //showNotification(saveFile);
        //6: get the new URI of the file after it has been copied to save it in Room
        //Uri fileUri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authorities), saveFile);
        return saveFile.getAbsolutePath();//fileUri;
    }


    public static String getImageExtension(Context context, Uri uri) {
        ContentResolver contentResolver=context.getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        // Return file Extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    public static String getRealPathFromImage(Context context, Uri selectedImageUri) {
        String selectedImagePath = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(selectedImageUri,
                projection, null, null, null);
        if (cursor == null) {
            selectedImagePath = selectedImageUri.getPath();
        } else {
            if (!cursor.moveToFirst()) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                selectedImagePath = cursor.getString(idx);
            }
        }
        return selectedImagePath;
    }



    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        // We ask for the bounds if they have been set as they would be most
        // correct, then we check we are  > 0
        final int width = !drawable.getBounds().isEmpty() ?
                drawable.getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ?
                drawable.getBounds().height() : drawable.getIntrinsicHeight();

        // Now we check we are > 0
        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public static int[] imagePickedWidthHeight(Context context, Uri uri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        AssetFileDescriptor fileDescriptor = null;
        Bitmap bitmap =null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bitmap!=null) {
            Timber.d("image sized - width: "+ bitmap.getWidth() +" height: "+bitmap.getHeight());
            return new int[] {bitmap.getWidth(),bitmap.getHeight()};
        }
        else
            return new int[] {0,0};

    }

    public static Uri getUriOfImageCroppedSaved(){
        return mUriOfImageCroppedSaved;
    }


}
*/

