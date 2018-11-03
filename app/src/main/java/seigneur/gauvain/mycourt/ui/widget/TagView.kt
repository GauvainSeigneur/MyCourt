package seigneur.gauvain.mycourt.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import seigneur.gauvain.mycourt.R

class TagView : ConstraintLayout {
    private var tagView: ConstraintLayout? = null
    private var tag: TextView? = null
    private var closeIcon: ImageView? = null
    private var mText: String? = null

    //Attributes
    internal var backgroundColor: Int = 0
    internal var defaultBackgroundColor: Int = 0
    internal var textColor: Int = 0
    internal var defaultTextColor: Int = 0
    internal var textSize: Float = 0.toFloat()
    internal var isCloseIconVisible: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TagView)
        if (a.hasValue(R.styleable.TagView_backgroundColor)) {
            backgroundColor = a.getColor(R.styleable.TagView_backgroundColor, defaultBackgroundColor)
        }
        if (a.hasValue(R.styleable.TagView_textColor)) {
            textColor = a.getColor(R.styleable.TagView_textColor, defaultTextColor)
        }
        if (a.hasValue(R.styleable.TagView_android_text)) {
            mText = a.getString(R.styleable.TagView_android_text)
        }
        if (a.hasValue(R.styleable.TagView_textSize)) {
            textSize = a.getDimensionPixelSize(R.styleable.TagView_textSize, 0).toFloat()
        } else {
            textSize = context.resources.getDimensionPixelSize(R.dimen.default_tag_text_size).toFloat()
        }
        if (a.hasValue(R.styleable.TagView_showCloseIcon)) {
            isCloseIconVisible = a.getBoolean(R.styleable.TagView_showCloseIcon, true)
        }
        a.recycle()
        init()
        tagView!!.background.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP)
        closeIcon!!.drawable.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP)
        if (isCloseIconVisible)
            closeIcon!!.visibility = View.VISIBLE
        else
            closeIcon!!.visibility = View.GONE
        //tag.setTextSize(textSize);
        tag!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize / resources.displayMetrics.scaledDensity)
        tag!!.setTextColor(textColor)
        tag!!.text = mText
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.tag_view, this)
        this.tagView = findViewById(R.id.tag_view)
        this.tag = findViewById(R.id.tag_title)
        this.closeIcon = findViewById(R.id.tag_close_icon)
    }

    fun setText(text: CharSequence) {
        tag!!.text = text
        invalidate()
    }
}