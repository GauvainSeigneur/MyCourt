package seigneur.gauvain.mycourt.ui.pin.view;


import android.os.Bundle;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.ui.pin.presenter.PinPresenter;

public class PinActivity extends BaseActivity implements PinView {

    @Inject
    PinPresenter mPinPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.activity_pin);
        mPinPresenter.onAttach();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPinPresenter.onDetach();
    }
}
