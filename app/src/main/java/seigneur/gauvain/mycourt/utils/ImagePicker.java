package seigneur.gauvain.mycourt.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class ImagePicker {

    private static final String TAG = "ImagePicker";
    private static final String TEMP_IMAGE_NAME = "tempImage";

    public static void pickImage(Activity activity, int requestCode) {
        activity.startActivityForResult(
                createChooserIntent(),
                requestCode);
    }

    private static Intent createChooserIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return Intent.createChooser(intent, null);
    }

    public static String getImageNameFromResult(Context context, int resultCode) {
        Log.d(TAG, "getImageFromResult, resultCode: " + resultCode);
        File imageFile = getTempFile(context);
        String imageName = null;
        if (resultCode == Activity.RESULT_OK) {
            imageName = imageFile.getName();
        }
        return imageName;
    }

    public static Uri getImageUriFromResult(Context context, int resultCode, Intent imageReturnedIntent) {
        Log.d(TAG, "getImageFromResult, resultCode: " + resultCode);
        File imageFile = getTempFile(context);
        Uri selectedImage = null;
        if (resultCode == Activity.RESULT_OK) {
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null  ||
                    imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /** CAMERA **/
                selectedImage = Uri.fromFile(imageFile);
            } else {            /** ALBUM **/
                selectedImage = imageReturnedIntent.getData();
            }
            Log.d(TAG, "selectedImage: " + selectedImage);
        }
        return selectedImage;
    }

    private static File getTempFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }

}
