package seigneur.gauvain.mycourt.ui.shotEdition.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface EditShotPresenter extends BasePresenter {

    void onIllustrationClicked();

    void onConfirmEditionClicked(boolean isFromFab);

    void onImagePicked(Context context, int requestCode, int resultCode, Intent data);

    void onPermissionGranted(Context context);

    void onPermissionDenied();

    void onImageCropped(int requestCode,int resultCode, Intent data);

    void onTagLimitReached();

    void onPublishClicked(Context context);

    void onDraftShotClicked(Context context);

    void onAbort(boolean isMenuOpen);

    void onTitleChanged(String title);

    void onDescriptionChanged(String description);

    void onTagChanged(String tags);

}

