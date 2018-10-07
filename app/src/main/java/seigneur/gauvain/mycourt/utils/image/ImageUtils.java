package seigneur.gauvain.mycourt.utils.image;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import seigneur.gauvain.mycourt.R;
import timber.log.Timber;

public class ImageUtils {

    public static Uri mUriOfImageCroppedSaved;

    public ImageUtils(){}

    /**
     * Go to cropping activity
     * @param ImageCroppedFormat -  PNG, JPEG, GIF
     * @param source of the view  - set by the return intent from ImagePicker
     * @param destination - uri of destination folder: cacheDif
     * @param activity - context
     * @param imageSize - height and width in pixels
     */
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
        //Dribbble specifications
        if (ImageCroppedFormat!=null && ImageCroppedFormat.equals("gif")){
            if (imageSize[0]==800 && imageSize[1]==600 || imageSize[0]==400
                    && imageSize[1]==300 ) {
                options.setHideBottomControls(true);
                options.setAllowedGestures(0,0,0);
                UCrop.of(source, destination)
                        .useSourceImageAspectRatio() //use source aspect ratio
                        .withOptions(options)
                        .start(activity);
                Toast.makeText(activity, "gif can't be cropped", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "gif can't be cropped. the size must be...", Toast.LENGTH_SHORT).show();
            }
        } else {
            //HD image set withMaxResultSize in accordance
            if (imageSize[0]>=800 && imageSize[1]>=600) {
                UCrop.of(source, destination)
                        .withAspectRatio(4, 3)
                        .withOptions(options)
                        .withMaxResultSize(800, 600)//set in in constants
                        .start(activity);
                Timber.d("crop hd mode");
            }
            else {
                //NORMAL image set withMaxResultSize in accordance
                UCrop.of(source, destination)
                        .withAspectRatio(4, 3)
                        .withOptions(options)
                        .withMaxResultSize(400, 300) //set in in constants
                        .start(activity);
                Timber.d("crop normal mode");
            }
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

    /**
     * get extension of Image from Image picker intent result
     * @param uri - uri the image selected by user
     * @return the format. must be JPG, PNG or GIF. Nothing else
     */
    public static String getImageExtension(Context context, Uri uri) {
        ContentResolver contentResolver=context.getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        // Return file Extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * Get the the file system path from the content provider uri
     * @param context - activity or fragment
     * @param selectedImageUri - Uri of the image
     * @return the system file url (get the file)
     */
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


    /**
     * Transform image to bitmap
     * @param drawable - resource load from Glide
     * @return the bitmap
     */
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


    /**
     * Get the image height and width to
     * @param context
     * @param uri
     * @param sampleSize
     * @return
     */
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

    public static Bitmap decodeBitmap(Context context, Uri uri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        AssetFileDescriptor fileDescriptor = null;
        Bitmap actuallyUsableBitmap = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Timber.d("error");
        }
        return actuallyUsableBitmap;
    }

}
