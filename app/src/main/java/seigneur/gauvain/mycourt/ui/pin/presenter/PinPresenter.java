package seigneur.gauvain.mycourt.ui.pin.presenter;

import seigneur.gauvain.mycourt.ui.base.BasePresenter;

public interface PinPresenter extends BasePresenter {

    void onPinConfirmed(String pin);

    void onCheckPinSuccess();

    void onCheckPinFailed();

    void onPinReady();

}

