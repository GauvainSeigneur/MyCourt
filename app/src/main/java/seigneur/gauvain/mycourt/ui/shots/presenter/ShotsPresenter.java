package seigneur.gauvain.mycourt.ui.shots.presenter;

import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface ShotsPresenter extends BasePresenter {

    void onLoadFirstPage(int page);

    void onLoadNextPage(int page);

    void onLoadRefresh(int page);

    void onShotClicked(Shot shot, int position);

    boolean isLastPageReached();

    boolean isLoading();

    void onLoading();

}

