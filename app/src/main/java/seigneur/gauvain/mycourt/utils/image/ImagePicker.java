package seigneur.gauvain.mycourt.utils.image;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;


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

    /*public static String getImageNameFromResult(Context context, int resultCode) {
        File imageFile = getTempFile(context);
        String imageName = null;
        if (resultCode == Activity.RESULT_OK) {
            imageName = imageFile.getName();
        }
        return imageName;
    }*/

    /**
     * get the file name of the picked Image
     * @param context - context provided by activity
     * @param uri - uri from intent data
     * @return file name
     */
    public static String getPickedImageName(Context context, Uri uri) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        String nameWithoutExtenstion;
        if (name!=null) {
            nameWithoutExtenstion = name.substring(0, name.lastIndexOf('.'));
        }
        else {
            File imageFile = getTempFile(context);
            nameWithoutExtenstion = imageFile.getName();
        }

        return nameWithoutExtenstion;
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
        }
        return selectedImage;
    }

    private static File getTempFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }

}
