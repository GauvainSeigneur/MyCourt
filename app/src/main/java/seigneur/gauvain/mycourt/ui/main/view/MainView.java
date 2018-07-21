package seigneur.gauvain.mycourt.ui.main.view;

public interface MainView {

    void goBackAtStart(int position);

    void showFragment(int position);

    void goToShotEdition();

    void showMessageShotPublished();

    void showMessageShotDrafted();

    void showNoInternetConnectionMessage(boolean showIt);

    void showInternetConnectionRetrieved(boolean showIt);

}
