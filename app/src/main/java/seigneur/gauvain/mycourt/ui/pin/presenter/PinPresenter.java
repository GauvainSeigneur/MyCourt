package seigneur.gauvain.mycourt.ui.pin.presenter;

import seigneur.gauvain.mycourt.ui.base.mvp.BaseMVPView;
import seigneur.gauvain.mycourt.ui.base.mvp.BasePresenter;

public interface PinPresenter<V extends BaseMVPView> extends BasePresenter<V> {

    void onViewReady();

    void onFirstPinDefined(String pin);

    void onNewPinConfirmed(String pin);

    void onCurrentPinConfirmed(String pinInput);

    void onCheckPinSuccess();

    void onCheckPinFailed();


}

