package seigneur.gauvain.mycourt.ui.shotDetail.attachments

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
import timber.log.Timber


class AttachmentPreviewVH private constructor(itemView: View) :
        BaseViewHolder(itemView), View.OnClickListener {

    @BindView(R.id.attachment_preview)
    lateinit var mAttachmentPreview: ImageView

    private val glideOptions = RequestOptions()

    init {
        mAttachmentPreview.setOnClickListener(this)
        glideOptions.centerCrop()
        glideOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
    }

    fun bindTo(attachment: Attachment) {
        Timber.d("attachment format:" +attachment.contentType)
        if (attachment.contentType!!.contains("image")) {
            Glide.with(itemView.context)
                    .load(attachment.uri)
                    .apply (glideOptions)
                    .into(mAttachmentPreview)
        } else {
            //todo -change it
            mAttachmentPreview.setImageResource(R.drawable.icon_file_doc)
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
        }
    }

    companion object {
        fun create(parent: ViewGroup): AttachmentPreviewVH {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_detail_attachment_preview, parent, false)
            return AttachmentPreviewVH(view)
        }
    }

}
