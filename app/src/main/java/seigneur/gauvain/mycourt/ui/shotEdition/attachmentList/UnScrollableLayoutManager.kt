package seigneur.gauvain.mycourt.ui.shotEdition.attachmentList

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

open class UnScrollableLayoutManager(context: Context, spanCount: Int) :
        GridLayoutManager(context,spanCount) {
    private var scrollable = true

    fun enableScrolling() {
        scrollable = true
    }

    fun disableScrolling() {
        scrollable = false
    }

    override fun canScrollVertically() =
            super.canScrollVertically() && scrollable


    override fun canScrollHorizontally() =
            super.canScrollVertically()

                    && scrollable
}