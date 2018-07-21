package seigneur.gauvain.mycourt.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.Single;
import seigneur.gauvain.mycourt.R;
import timber.log.Timber;

public class ImageUtils {

    public static Uri mUriOfImageCroppedSaved;

    public ImageUtils(){}

    /**
     * Go to cropping activity
     * @param ImageCroppedFormat
     * @param source of the view :set by the return intent from ImagePicker
     * @param destination : uri of destination folder: cacheDif
     * @param activity
     */
    public static void goToUCropActivity(String ImageCroppedFormat, Uri source, Uri destination, Activity activity) {
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));
        options.setToolbarColor(activity.getResources().getColor(R.color.colorPrimary));
        //todo : refactor this! it does not prevent issue!
        if (ImageCroppedFormat!=null && ImageCroppedFormat.equals("gif")){
            options.setHideBottomControls(true);
            options.setAllowedGestures(0,0,0);
            UCrop.of(source, destination)
                    .useSourceImageAspectRatio() //use source aspect ratio
                    .withOptions(options)
                    //.withMaxResultSize(maxWidth, maxHeight)
                    .start(activity);
            Toast.makeText(activity, "gif can't be cropped", Toast.LENGTH_SHORT).show();
        } else {
            UCrop.of(source, destination)
                    .withAspectRatio(4, 3)
                    .withOptions(options)
                    //.withMaxResultSize(maxWidth, maxHeight)
                    .start(activity);
        }
    }

    /**
     * Return Uri of Image after being saved in gallery
     * Must use Single or Observable to get the Uri returned
     */
    public static Uri saveImageAndGetItsFinalUri(String ImageCroppedFormat, Uri croppedFileUri, Context context) throws Exception {
        Timber.d(croppedFileUri.getLastPathSegment());
        //1: define path and create folder "MyCourt" inside Gallery
        String appDirectoryName = "MyCourt";
        File myCourtDraftFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), appDirectoryName);
        myCourtDraftFolder.mkdirs();
        //2: give to image a name
        String filename = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment())+"."+ImageCroppedFormat;
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
        Uri fileUri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authorities), saveFile);
        return fileUri;
        //return setUriOfImageCroppedSaved(fileUri);
        //close activity
        //context.finish();
    }

    /**
     * Simple void to store image in Gallery (must no used)
     * Must use completable
     */
    public static void copyFileToGallery(String ImageCroppedFormat,Uri croppedFileUri, Context context) throws Exception {
        Timber.d(croppedFileUri.getLastPathSegment());
        //1: define path and create folder "MyCourt" inside Gallery
        String appDirectoryName = "MyCourt";
        File myCourtDraftFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), appDirectoryName);
        myCourtDraftFolder.mkdirs();
        //2: give to image a name
        String filename = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment())+"."+ImageCroppedFormat;
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
        Uri fileUri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authorities), saveFile);
        setUriOfImageCroppedSaved(fileUri);
        //close activity
        //context.finish();
    }


    /**
     * get the uri of the saved cropped image: to be used to save ShotDraft object in Room
     * @return UriOfImageCroppedSaved
     */
    public static Uri getUriOfImageCroppedSaved(){
        return mUriOfImageCroppedSaved;
    }

    /**
     * set UriOgImageCropped
     * @param uriOfImageCroppedSaved
     */
    public static void setUriOfImageCroppedSaved(Uri uriOfImageCroppedSaved) {
        mUriOfImageCroppedSaved = uriOfImageCroppedSaved;
    }

    /**
     * get extension of Image from Image picker intent result
     * @param uri of the image selected by user
     * @return the format. must be JPG, PNG or GIF. Nothing else
     */
    public static String getImageExtension(Uri uri, Context context) {
        ContentResolver contentResolver=context.getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        // Return file Extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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



}
