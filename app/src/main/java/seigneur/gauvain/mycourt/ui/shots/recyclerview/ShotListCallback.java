package seigneur.gauvain.mycourt.ui.shots.recyclerview;

import seigneur.gauvain.mycourt.data.model.Shot;

public interface ShotListCallback {

    void onShotClicked(Shot shot, int  position);

    void retryPageLoad();
}