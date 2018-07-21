package seigneur.gauvain.mycourt.ui.shotDetail.view;

import android.graphics.drawable.Drawable;

import seigneur.gauvain.mycourt.data.model.Shot;

public interface ShotDetailView {

   void showErrorView(boolean visible);

   void loadShotImage(Shot shot);

   //show color palette of shot if available
   void showPaletteShot(boolean isVisible);

   void adaptColorToShot(Drawable resource);

   void initImageScrollBehavior();

   void setUpShotInfo(Shot shot);

   void showEditionResult(int result);

   void goToShotEdition();

   void startPosponedEnterTransition();

}
