package seigneur.gauvain.mycourt.utils

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

object ViewUtils {

    //need to cast ViewOutlineProvider() as object in kotlin because we want to slight modification of
    // it, without explicitly declaring a new subclass for (override fun here)
    val CIRCULAR_OUTLINE: ViewOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setOval(view.paddingLeft,
                    view.paddingTop,
                    view.width - view.paddingRight,
                    view.height - view.paddingBottom)
        }
    }
}
