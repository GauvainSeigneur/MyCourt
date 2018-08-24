package seigneur.gauvain.mycourt.ui.shotDraft.presenter;

import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface ShotDraftPresenter extends BasePresenter {

   /**
    * Swipe refresh layout onRefresh called
    */
   void onRefresh(boolean fromSwipeRefresh);

   /**
    * User has clicked on a ShotDraft item in the draft list
    * @param shotDraft  - ShotDraft object clicked
    * @param position   - Position of the clicked item in the list
    */
   void onShotDraftClicked(ShotDraft shotDraft, int position);

}

