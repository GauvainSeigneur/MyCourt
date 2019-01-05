package seigneur.gauvain.mycourt.ui.shotEdition.attachmentList


interface AttachmentItemCallback {

    fun onAddClicked()

    fun onAttachmentClicked(position: Int)

    fun onAttachmentDeleted(position: Int)

}
