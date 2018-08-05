package seigneur.gauvain.mycourt.ui.main.presenter;

import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface MainPresenter extends BasePresenter {

    void onBottomNavItemSelected(int position);

    void onBottomNavItemReselected(int position);

    void onAddFabclicked();

    void onReturnShotDrafted();

    void onReturnShotPublished();

    void onCheckInternetConnection();

    void onReturnFromDraftPublishing();

    /**
     *  Sometimes, after a long pause, the data are deleted by Android, so we cannot perform
     *  any api operation. so check if user is null on resume and get it again from DB
     */
    void checkIfTokenIsNull();
}
