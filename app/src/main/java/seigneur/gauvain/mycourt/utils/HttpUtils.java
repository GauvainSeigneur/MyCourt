package seigneur.gauvain.mycourt.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import seigneur.gauvain.mycourt.utils.image.ImageUtils;

public class HttpUtils {

    public HttpUtils(){}

    /**
     * Create MultipartBody.Part instance separated in order to use @PartMap annotation
     * to pass parameters along with File request. PartMap is a Map of "Key" and RequestBody.
     * See : https://stackoverflow.com/a/40873297
     */
    public static MultipartBody.Part createFilePart(Context context, Uri fileUri, String imageFormat, String partName) {
        //Get file
        String uriOfFile = ImageUtils.getRealPathFromImage(context,fileUri);
        File file = new File(uriOfFile);
        String imageFormatFixed;
        //Word around for jpg format - refused by dribbble
        if (imageFormat.equals("jpg")) imageFormatFixed="JPG"; // to be tested
        else imageFormatFixed =imageFormat;
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/"+imageFormatFixed), file);
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
}
