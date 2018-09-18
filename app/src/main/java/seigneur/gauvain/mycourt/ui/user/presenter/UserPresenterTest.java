package seigneur.gauvain.mycourt.ui.user.presenter;

import seigneur.gauvain.mycourt.ui.base.BaseMVPView;
import seigneur.gauvain.mycourt.ui.base.BasePresenter;
import seigneur.gauvain.mycourt.ui.base.BasePresenterTest;

public interface UserPresenterTest<V extends BaseMVPView> extends BasePresenterTest<V> {

    //Move inside BasePresenter after testing it
    void onViewReady();

}

