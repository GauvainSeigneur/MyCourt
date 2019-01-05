package seigneur.gauvain.mycourt.ui.shotEdition.attachmentList

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState

class AttachmentsAdapter(private val attachmentItemCallback: AttachmentItemCallback)
    : PagedListAdapter<Shot, RecyclerView.ViewHolder>(UserDiffCallback) {

    private var networkState: NetworkState? = null

    private val pos: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM -> return AttachmentViewHolder.create(parent, attachmentItemCallback)
            ADD -> return AddAttachmentViewHolder.create(parent, attachmentItemCallback)
            else -> throw IllegalArgumentException("unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> (holder as AttachmentViewHolder).bindTo("todo")
            ADD -> (holder as AddAttachmentViewHolder).bindTo(hasExtraRow())
        }
    }

    private fun hasExtraRow(): Boolean {
        return true //todo - define rule here
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            ADD
        } else {
            ITEM
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun getShotClicked(pos: Int): Shot? {
        return getItem(pos)
    }

    /**
     * Set the current network state to the adapter
     * but this work only after the initial load
     * and the adapter already have list to add new loading raw to it
     * so the initial loading state the activity responsible for handle it
     *
     * @param newNetworkState the new network state
     */
    fun setNetworkState(newNetworkState: NetworkState) {
        if (currentList != null) {
            if (currentList!!.size != 0) {
                val previousState = this.networkState
                val hadExtraRow = hasExtraRow()
                this.networkState = newNetworkState
                val hasExtraRow = hasExtraRow()
                if (hadExtraRow != hasExtraRow) {
                    if (hadExtraRow) {
                        notifyItemRemoved(super.getItemCount())
                    } else {
                        notifyItemInserted(super.getItemCount())
                    }
                } else if (hasExtraRow && previousState != newNetworkState) {
                    notifyItemChanged(itemCount - 1)
                }
            }
        }
    }

    companion object {

        val ITEM = 0
        val ADD = 1

        private val UserDiffCallback = object : DiffUtil.ItemCallback<Shot>() {
            override fun areItemsTheSame(oldItem: Shot, newItem: Shot): Boolean {
                return oldItem.id === newItem.id
            }

            override fun areContentsTheSame(oldItem: Shot, newItem: Shot): Boolean {
                return oldItem == newItem
            }
        }
    }


}