package seigneur.gauvain.mycourt.ui.shotDetail.attachments

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import seigneur.gauvain.mycourt.data.model.Attachment

class AttachmentListAdapter(private val context: Context, private val attachments: List<Attachment>)
    : RecyclerView.Adapter<AttachmentPreviewVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentPreviewVH {
        val Vh = AttachmentPreviewVH.create(parent)
        return Vh
    }

    override fun onBindViewHolder(holder: AttachmentPreviewVH, position: Int) {
        holder.bindTo(attachments[position])
    }

    override fun getItemCount(): Int {
        return attachments.size
    }
}
