package seigneur.gauvain.mycourt.ui.shotEdition.attachmentList

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import seigneur.gauvain.mycourt.data.model.Attachment
import seigneur.gauvain.mycourt.data.model.Draft


import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState

class AttachmentsAdapter(
        private val data: MutableList<Attachment>,
        private val attachmentItemCallback: AttachmentItemCallback) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val pos: Int = 0
    var mAddAttachmentVh:AddAttachmentViewHolder?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM -> return AttachmentViewHolder.create(parent, attachmentItemCallback)
            ADD -> {
                 mAddAttachmentVh = AddAttachmentViewHolder.create(parent, attachmentItemCallback)
                 return mAddAttachmentVh!!
            }
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> (holder as AttachmentViewHolder).bindTo(data[position])
            ADD -> (holder as AddAttachmentViewHolder)

        }
    }

    fun updateRv() {
        if (data.size<5) {
            mAddAttachmentVh?.seVisible(true)
        } else {
            mAddAttachmentVh?.seVisible(false)
        }
        notifyDataSetChanged()//finally update ui
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            ADD
        } else {
            ITEM
        }
    }

    override fun getItemCount(): Int {
        return data.size  + 1
    }

    companion object {
        val ITEM = 0
        val ADD = 1
    }
}