package seigneur.gauvain.mycourt.ui.base;

import android.view.View;

/**
 * Base presenter to not duplicate some global methods
 * ONLY UI, NOT DI
 */
public interface BasePresenterTest<V extends BaseMVPView> {

    /**
     * Presenter is attached to View
     */
    void onAttach(V view);

     /**
      * Presenter is detached from view
     */
    void onDetach();

}


