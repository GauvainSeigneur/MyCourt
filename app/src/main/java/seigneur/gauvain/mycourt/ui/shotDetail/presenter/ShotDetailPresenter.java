package seigneur.gauvain.mycourt.ui.shotDetail.presenter;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface ShotDetailPresenter extends BasePresenter {

    void onEditShotClicked();

    void onShotImageAvailable(boolean isResourceReady,@Nullable Drawable resource);

    /**
     * Result code must be : todo - add numbers in constant field
     * 0 :  return on Detail with update success on server (display new data!)
     * 1 :  return on detail with edit saved in draft (not published)
     * 2 :  return without publishing or save edition...
     */
    void OnReturnFromEdition(int resultCode);


}

