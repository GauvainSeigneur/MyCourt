package seigneur.gauvain.mycourt.utils;

import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

public class ViewUtils {
    private ViewUtils() { }

    public static final ViewOutlineProvider CIRCULAR_OUTLINE = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setOval(view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getWidth() - view.getPaddingRight(),
                    view.getHeight() - view.getPaddingBottom());
        }
    };
}
