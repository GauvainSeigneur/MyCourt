package seigneur.gauvain.mycourt.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import seigneur.gauvain.mycourt.R;

public class TagView extends ConstraintLayout {
    private ConstraintLayout tagView;
    private TextView tag;
    private ImageView closeIcon;
    private String mText;

    //Attributes
    int backgroundColor;
    int defaultBackgroundColor;
    int textColor;
    int defaultTextColor;
    float textSize;
    boolean isCloseIconVisible;

    public TagView(Context context) {
        super(context);
        init();
    }

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TagView);
        if (a.hasValue(R.styleable.TagView_backgroundColor)) {
            backgroundColor = a.getColor(R.styleable.TagView_backgroundColor, defaultBackgroundColor);
        }
        if (a.hasValue(R.styleable.TagView_textColor)) {
            textColor = a.getColor(R.styleable.TagView_textColor, defaultTextColor);
        }
        if (a.hasValue(R.styleable.TagView_android_text)) {
            mText = a.getString(R.styleable.TagView_android_text);
        }
        if (a.hasValue(R.styleable.TagView_textSize)) {
            textSize = a.getDimensionPixelSize(R.styleable.TagView_textSize,0);
        } else {
            textSize= context.getResources().getDimensionPixelSize(R.dimen.default_tag_text_size);
        }
        if (a.hasValue(R.styleable.TagView_showCloseIcon)) {
            isCloseIconVisible = a.getBoolean(R.styleable.TagView_showCloseIcon,true);
        }
        a.recycle();
        init();
        tagView.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
        closeIcon.getDrawable().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
        if(isCloseIconVisible)
            closeIcon.setVisibility(VISIBLE);
        else
            closeIcon.setVisibility(GONE);
        //tag.setTextSize(textSize);
        tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, (textSize / getResources().getDisplayMetrics().scaledDensity));
        tag.setTextColor(textColor);
        tag.setText(mText);
    }

    public TagView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.tag_view, this);
        this.tagView = (ConstraintLayout) findViewById(R.id.tag_view);
        this.tag = (TextView) findViewById(R.id.tag_title);
        this.closeIcon = (ImageView) findViewById(R.id.tag_close_icon);
    }

    public void setText(CharSequence text) {
        tag.setText(text);
        invalidate();
    }
}