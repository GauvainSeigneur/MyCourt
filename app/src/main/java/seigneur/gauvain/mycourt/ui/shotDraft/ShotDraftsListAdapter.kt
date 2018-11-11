package seigneur.gauvain.mycourt.ui.shotDraft

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.ui.main.CustomBottomActionMode


/**
 * Constructor
 * @param context   - activity
 * @param data      - list of ShotDraft object
 * @param callback  - ShotDraftListCallback implementation
 */
class ShotDraftsListAdapter(private val context: Context,
                            private val data: MutableList<Draft>,
                            private var mCallback: ShotDraftListCallback,
                            private var mDraftListEditMode: CustomBottomActionMode.DraftListEditMode)
    : RecyclerView.Adapter<ShotDraftViewHolder>() {

    val selectedIds: MutableList<Long> = ArrayList<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShotDraftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_post, parent, false)
        return ShotDraftViewHolder(view, mCallback)
    }

    override fun onBindViewHolder(holder: ShotDraftViewHolder, position: Int) {
        val item = data[position]
        manageSelection(holder,position)
        holder.bindTo(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun clear() {
        while (itemCount > 0) {
            data.clear()
        }
    }

    fun addIDIntoSelectedIds(index: Int) {
        val id = data[index].draftID
        if (selectedIds.contains(id))
            selectedIds.remove(id)
        else
            selectedIds.add(id)
        notifyItemChanged(index)
    }

    private fun manageSelection(holder: ShotDraftViewHolder, position: Int) {
        val id = data[position].draftID
        if (selectedIds.contains(id)) {
            //if item is selected then,set foreground color
            holder.shotDraftLayout.foreground = ColorDrawable(ContextCompat.getColor(context, R.color.colorSelectedForeground))
        } else  {
            //else remove selected item color.
            holder.shotDraftLayout.foreground = ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent))
        }
    }

    fun deleteSelectedIds() {
        if (selectedIds.size < 1) return
        val selectedIdIteration = selectedIds.listIterator()
        while (selectedIdIteration.hasNext()) {
            val selectedItemID = selectedIdIteration.next()
            var indexOfModelList = 0
            val modelListIteration: MutableListIterator<Draft> = data.listIterator()
            while (modelListIteration.hasNext()) {
                val model = modelListIteration.next()
                if (selectedItemID.equals(model.draftID)) {
                    modelListIteration.remove()
                    selectedIdIteration.remove()
                    notifyItemRemoved(indexOfModelList)
                }
                indexOfModelList++
            }
            //todo finish action mode in fragment/activity
           // MainActivity.isMultiSelectOn = false
        }
    }


}
