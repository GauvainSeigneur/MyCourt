package seigneur.gauvain.mycourt.ui.shotDraft.presenter;

import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface ShotDraftPresenter extends BasePresenter {

   void onRefresh();

   void onShotDraftClicked(ShotDraft shotDraft, int position);

}

