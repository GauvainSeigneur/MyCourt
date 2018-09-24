package seigneur.gauvain.mycourt.ui.base;

/**
 * Base presenter to not duplicate some global methods
 * ONLY UI, NOT DI
 */
public interface BasePresenter {

    /**
     * Presenter is attached to View
     */
    void onAttach();

     /**
      * Presenter is detached from view
     */
    void onDetach();

}


