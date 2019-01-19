package seigneur.gauvain.mycourt.ui.shotDetail.tags

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import seigneur.gauvain.mycourt.R

/**
 * RecyclerView Adapter for Shot's tags
 */
class TagListAdapter(private val context: Context, private val tags: List<String>) : RecyclerView.Adapter<TagViewHolder>() {
    //var tagViewHolder: TagViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.tag.setText(tags[position])
    }

    override fun getItemCount(): Int {
        return tags.size
    }
}
