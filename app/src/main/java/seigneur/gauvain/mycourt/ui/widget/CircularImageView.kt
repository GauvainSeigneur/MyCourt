package seigneur.gauvain.mycourt.ui.widget


import android.content.Context
import android.util.AttributeSet

import seigneur.gauvain.mycourt.utils.ViewUtils

/**
 * An extension to image view that has a circular outline.
 */
class CircularImageView(context: Context, attrs: AttributeSet) : ForegroundImageView(context, attrs) {

    init {
        outlineProvider = ViewUtils.CIRCULAR_OUTLINE
        clipToOutline = true
    }
}
