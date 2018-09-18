package seigneur.gauvain.mycourt.ui.pin.view;


import seigneur.gauvain.mycourt.ui.base.BaseMVPView;

public interface PinView extends BaseMVPView {

    void showCreationPinStep(int step);

    void showConfirmCurrentPinView(boolean isVisible);

}
