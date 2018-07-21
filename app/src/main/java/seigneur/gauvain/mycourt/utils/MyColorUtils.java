package seigneur.gauvain.mycourt.utils;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.view.View;

public class MyColorUtils {

    public MyColorUtils() {
    }

    /**
     * todo-move it in utils
     * Check if color is dark or light
     * @param color
     * @return true if is it dark
     */
    public static boolean isDark(int color) {
        return ColorUtils.calculateLuminance(color) < 0.75;
    }

    public static void setLightStatusBar(View view){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void clearLightStatusBar(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    /**
     * Lightens a color by a given factor.
     *
     * @param color: The color to lighten
     * @param factor: The factor to lighten the color. 0 will make the color unchanged.
     *            1 will make the color white.
     * @return lighter version of the specified color.
     */
    public static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }

}
