package seigneur.gauvain.mycourt.ui.shotEdition.view;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;

public interface EditShotView {

    void openConfirmMenu();

    void notifyPostSaved();

    void setUpShotEdtionUI(@Nullable Shot shot,
                           @Nullable ShotDraft shotDraft,
                           int editMode);

    void setUpShotCreationUI();

    void checkPermissionExtStorage();

    void requestPermission();

    void openImagePicker();

    void showImageNotUpdatable();

    void displayShotImagePreview(Uri uriImageCropped);

    void goToUCropActivity(String imagePickedFormat,
                           Uri ImagePickedUri,
                           String imagePickedFileName,
                           int[] imageSize);

    void showMessageEmptyTitle();

    void stopActivity();

}
