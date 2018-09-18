package seigneur.gauvain.mycourt.ui.shotEdition.presenter;

import android.content.Context;
import android.content.Intent;

import seigneur.gauvain.mycourt.ui.base.mvp.BaseMVPView;
import seigneur.gauvain.mycourt.ui.base.mvp.BasePresenter;

public interface EditShotPresenter<V extends BaseMVPView> extends BasePresenter<V> {

    void onViewReady();

    /**
     * User has clicked on image preview to change it
     */
    void onIllustrationClicked();

    /**
     * Edition being confirmed
     * @param isFromFab - boolean to check the source of the action (user, or app)
     */
    void onConfirmEditionClicked(boolean isFromFab);

    /**
     * Image was picked from picking activity
     * @param context       -   activity
     * @param requestCode   -   see constants
     * @param resultCode    -   needs to be OK
     * @param data          -   source of the image picked
     */
    void onImagePicked(Context context, int requestCode, int resultCode, Intent data);

    /**
     * Picked image move to Ucrop activity -  get the result and the uri of image cropped
     * @param requestCode   - see constants
     * @param resultCode   - needs to be ok
     * @param data         - source of the image picked
     */
    void onImageCropped(int requestCode,int resultCode, Intent data);

    /**
     * User wants to copy image cropped in external storage. Need to check permission
     * When permission granted, perform copy
     * @param context - activity
     */
    void onPermissionGranted(Context context);

    /**
     * User wants to copy image cropped in external storage. Need to check permission
     * Permission denied - ask to user permission or show a dedicated dialog to change
     * app settings
     */
    void onPermissionDenied();

    /**
     * Tags are limited to twelve
     */
    void onTagLimitReached();

    /**
     * User has clicked on publish button - perform POST or UPDATE
     * @param context - activity
     */
    void onPublishClicked(Context context);

    /**
     * User has clicked on draft button - perform DB registering or update
     * @param context - activity
     */
    void onDraftShotClicked(Context context);

    /**
     * User has clicked on back button
     * @param isMenuOpen - check if confirmEditionMenu is opened
     */
    void onAbort(boolean isMenuOpen);

    /**
     * User made changes on Title
     * @param title - string
     */
    void onTitleChanged(String title);

    /**
     * User made changes on description
     * @param description - string
     */
    void onDescriptionChanged(String description);

    /**
     * User made changes on tags
     * @param tags - string
     */
    void onTagChanged(String tags);

}

