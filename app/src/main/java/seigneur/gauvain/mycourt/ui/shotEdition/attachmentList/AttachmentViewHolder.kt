package seigneur.gauvain.mycourt.ui.shotEdition.attachmentList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide

import butterknife.BindView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Attachment
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder
import com.bumptech.glide.request.RequestOptions


class AttachmentViewHolder private constructor(itemView: View,
                                               private val attachmentItemCallback: AttachmentItemCallback) :
        BaseViewHolder(itemView), View.OnClickListener {

    @BindView(R.id.attachment_image)
    lateinit var mAttachmentPreview: ImageView

    @BindView(R.id.btn_delete_attachment)
    lateinit var mRemoveAttachment: ImageView

    private val glideOptions = RequestOptions()

    init {
        mAttachmentPreview.setOnClickListener(this)
        mRemoveAttachment.setOnClickListener(this)
        glideOptions.centerCrop()
        glideOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
    }

    fun bindTo(attachement: Attachment) {
        Glide.with(itemView.context)
                //.load(f)
                .load(attachement.uri)
                .apply (glideOptions)
                .into(mAttachmentPreview)
    }



    override fun onClick(view: View) {
        when (view.id) {
            R.id.attachment_image -> attachmentItemCallback.onAttachmentClicked(adapterPosition)
            R.id.btn_delete_attachment -> attachmentItemCallback.onAttachmentDeleted(adapterPosition)
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
