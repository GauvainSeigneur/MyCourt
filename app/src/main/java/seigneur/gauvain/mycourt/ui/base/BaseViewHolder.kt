package seigneur.gauvain.mycourt.ui.base

import androidx.recyclerview.widget.RecyclerView
import android.view.View

import butterknife.ButterKnife

/**
 * Base view holder for RecyclerView. Allows to bind views easily
 */
open class BaseViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    init {
        ButterKnife.bind(this, itemView)
    }
}
