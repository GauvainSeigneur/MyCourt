package seigneur.gauvain.mycourt.ui.pin.presenter;

import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface PinPresenter extends BasePresenter {

    void onFirstPinDefined(String pin);

    void onNewPinConfirmed(String pin);

    void onCurrentPinConfirmed(String pinInput);

    void onCheckPinSuccess();

    void onCheckPinFailed();


}

