package seigneur.gauvain.mycourt.ui.shots.list.adapter;


public interface ShotItemCallback {
    void retry();

    void onShotClicked(int position);
}
