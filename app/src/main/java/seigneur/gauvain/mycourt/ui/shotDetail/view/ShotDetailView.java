package seigneur.gauvain.mycourt.ui.shotDetail.view;

import android.graphics.drawable.Drawable;

import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.base.BaseMVPView;

public interface ShotDetailView extends BaseMVPView {

   /**
    * An error happened while trying to get shot object, show a message to user during
    * @param visible - show or hide the message
    */
   void showErrorView(boolean visible);

   /**
    * Load image from shot object
    * @param shot -  shot object to get the image url
    */
   void loadShotImage(Shot shot);

   /**
    * Show palette color of the shot
    * @param isVisible - show or hide
    */
   void showPaletteShot(boolean isVisible);

   /**
    * Adapt UI to the shot main color
    * @param resource - image load from glide request
    */
   void adaptColorToShot(Drawable resource);

   /**
    * Initialize image behavior according to collapsing Toolbar offset
    */
   void initImageScrollBehavior();

   /**
    * set up UI of the shot - called after the image is being loaded
    * @param shot - shot objet
    */
   void setUpShotInfo(Shot shot);

   /**
    * method called from edition callback
    * @param result - see constants
    */
   void showEditionResult(int result);

   /**
    * Got to EditShotActivity
    */
   void goToShotEdition();

   /**
    * postponed enter transition of activity from shot image availability
    */
   void startPosponedEnterTransition();

}
