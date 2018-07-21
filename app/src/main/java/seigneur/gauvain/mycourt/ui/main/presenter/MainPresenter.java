package seigneur.gauvain.mycourt.ui.main.presenter;

import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface MainPresenter extends BasePresenter {

    void onBottomNavItemSelected(int position);

    void onBottomNavItemReselected(int position);

    void onAddFabclicked();

    void onReturnShotDrafted();

    void onReturnShotPublished();

    void onCheckInternetConnection();
}
