package seigneur.gauvain.mycourt.ui.shotEdition.attachmentList

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide

import butterknife.BindView
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder

class AttachmentViewHolder private constructor(itemView: View,
                                               private val attachmentItemCallback: AttachmentItemCallback) :
        BaseViewHolder(itemView), View.OnClickListener {

    @BindView(R.id.attachment_image)
    lateinit var mAttachmentPreview: ImageView

    init {
        mAttachmentPreview.setOnClickListener(this)
    }

    fun bindTo(uri: String) {
        Glide.with(itemView.context)
                .load(uri)
                .into(mAttachmentPreview)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.attachment_image -> attachmentItemCallback.onAttachmentClicked(adapterPosition)
        }
    }

    companion object {
        fun create(parent: ViewGroup, attachmentItemCallback: AttachmentItemCallback): AttachmentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_attachment, parent, false)
            return AttachmentViewHolder(view, attachmentItemCallback)
        }
    }

}
