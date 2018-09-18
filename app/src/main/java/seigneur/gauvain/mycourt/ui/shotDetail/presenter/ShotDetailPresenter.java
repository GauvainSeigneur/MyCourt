package seigneur.gauvain.mycourt.ui.shotDetail.presenter;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import seigneur.gauvain.mycourt.ui.base.BaseMVPView;
import seigneur.gauvain.mycourt.ui.base.BasePresenter;
import seigneur.gauvain.mycourt.ui.base.BasePresenterTest;

public interface ShotDetailPresenter<V extends BaseMVPView> extends BasePresenterTest<V>  {

    void onViewReady();

    /**
     * User has clicked on button to modify the shot
     */
    void onEditShotClicked();

    /**
     * Callback from glide to know when the image is available to set up the activity UI
     * @param isResourceReady   - boolean, must be true
     * @param resource          - image
     */
    void onShotImageAvailable(boolean isResourceReady,@Nullable Drawable resource);

    /**
     * Result code must be : todo - add numbers in constant field
     * 0 :  return on Detail with update success on server (display new data!)
     * 1 :  return on detail with edit saved in draft (not published)
     * 2 :  return without publishing or save edition...
     */
    void OnReturnFromEdition(int resultCode);


}

