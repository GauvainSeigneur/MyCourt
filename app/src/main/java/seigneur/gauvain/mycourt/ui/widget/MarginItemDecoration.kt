package seigneur.gauvain.mycourt.ui.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val spaceTopBottom: Int,
                           private val spaceBetween: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceTopBottom
            }
            left =  spaceBetween
            right = spaceBetween
            bottom = spaceTopBottom
        }
    }
}