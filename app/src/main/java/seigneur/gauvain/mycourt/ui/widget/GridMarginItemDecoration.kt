package seigneur.gauvain.mycourt.ui.widget

import androidx.recyclerview.widget.RecyclerView
import android.R.attr.spacing
import android.R.attr.spacing
import android.graphics.Rect
import android.view.View


class GridMarginItemDecoration(spanCount: Int, spacing: Int, includeEdge: Boolean)
    : RecyclerView.ItemDecoration() {

    private val mSpanCount=spanCount
    private val mSpacing = spacing
   private val mIncludeEdge=includeEdge


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % mSpanCount // item column

        if (mIncludeEdge) {
            outRect.left = mSpacing - column * mSpacing / mSpanCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / mSpanCount // (column + 1) * ((1f / spanCount) * spacing)

            if (position < mSpanCount) { // top edge
                outRect.top = mSpacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left = column * mSpacing / mSpanCount // column * ((1f / spanCount) * spacing)
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= mSpanCount) {
                outRect.top = mSpacing // item top
            }
        }
    }

}