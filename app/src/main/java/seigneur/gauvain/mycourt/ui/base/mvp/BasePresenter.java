package seigneur.gauvain.mycourt.ui.base.mvp;

/**
 * Base presenter to not duplicate some global methods
 * ONLY UI, NOT DI
 */
public interface BasePresenter<V extends BaseMVPView> {

    /**
     * Presenter is attached to View
     */
    void onAttach(V view);

     /**
      * Presenter is detached from view
     */
    void onDetach();

}


