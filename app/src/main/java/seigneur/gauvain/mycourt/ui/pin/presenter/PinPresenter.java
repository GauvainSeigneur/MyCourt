package seigneur.gauvain.mycourt.ui.pin.presenter;

import seigneur.gauvain.mycourt.ui.base.BaseMVPView;
import seigneur.gauvain.mycourt.ui.base.BasePresenter;
import seigneur.gauvain.mycourt.ui.base.BasePresenterTest;

public interface PinPresenter<V extends BaseMVPView> extends BasePresenterTest<V> {

    void onViewReady();

    void onFirstPinDefined(String pin);

    void onNewPinConfirmed(String pin);

    void onCurrentPinConfirmed(String pinInput);

    void onCheckPinSuccess();

    void onCheckPinFailed();


}

