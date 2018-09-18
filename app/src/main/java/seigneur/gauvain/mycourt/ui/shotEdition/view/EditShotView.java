package seigneur.gauvain.mycourt.ui.shotEdition.view;

import android.net.Uri;

import seigneur.gauvain.mycourt.ui.base.mvp.BaseMVPView;

public interface EditShotView extends BaseMVPView {

    /**
     * show a menu which to publish or register the draft
     */
    void openConfirmMenu();

    /**
     * notify user that the draft has been saved
     */
    void notifyPostSaved();

    /**
     * set up UI according to date source
     * @param o         - Shot or ShotDraft
     * @param editMode  - see Constants file
     */
    void setUpShotEditionUI(Object o,
                           int editMode);

    /**
     * User wants to create a shot from scratch
     * Set UI in accordance
     */
    void setUpShotCreationUI();

    /**
     * User wants to copy a cropped image in My Court folder
     * Check permission to write on external storage
     */
    void checkPermissionExtStorage();

    /**
     * Request permission to write on external storage
     */
    void requestPermission();

    /**
     * Open activity to pick image on external storage
     */
    void openImagePicker();

    /**
     * User click on an image which can't be change. Notify the user
     */
    void showImageNotUpdatable();

    /**
     * Show image of the draft - from db or Dribbble
     * @param uriImageCropped - absolute url of the file
     */
    void displayShotImagePreview(Uri uriImageCropped);

    /**
     * Go to cropping activity after picking an image
     * @param imagePickedFormat     - jpg, png, gif
     * @param ImagePickedUri        - uri of the image to be cropped
     * @param imagePickedFileName   - name of the file
     * @param imageSize             - hd or normal, see Constants
     */
    void goToUCropActivity(String imagePickedFormat,
                           Uri ImagePickedUri,
                           String imagePickedFileName,
                           int[] imageSize);

    /**
     * Users wants to save a draft without give it a title. Warn the user
     */
    void showMessageEmptyTitle();

    /**
     * finish activity
     */
    void stopActivity();

}
