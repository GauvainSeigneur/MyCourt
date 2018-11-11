package seigneur.gauvain.mycourt.ui.main

class CustomBottomActionMode(private var mDraftListEditMode: DraftListEditMode) {

    fun startMode() {
        mDraftListEditMode.onListEditStart()
    }

    fun stopMode() {
        mDraftListEditMode.onListEditStop()
    }

    interface DraftListEditMode {

        fun onListEditStart()

        fun onListEditStop()

    }

}

