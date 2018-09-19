package seigneur.gauvain.mycourt.ui.user.presenter;

import seigneur.gauvain.mycourt.ui.base.mvp.BaseMVPView;
import seigneur.gauvain.mycourt.ui.base.mvp.BasePresenter;

public interface UserPresenter<V extends BaseMVPView> extends BasePresenter<V> {

    //Move inside BasePresenter after testing it
    void onViewReady();

}

