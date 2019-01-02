package seigneur.gauvain.mycourt.utils

import android.content.Context
import android.content.res.Resources
import com.google.android.material.appbar.AppBarLayout
import android.util.DisplayMetrics
import android.util.TypedValue
import io.reactivex.internal.operators.flowable.FlowableTakeLastOne

object MathUtils {

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    @JvmStatic
    fun convertDpToPixel(dp:Float, context:Context):Float{
        val res = context.resources
        val metrics = res.displayMetrics
        val densityOfScreen = metrics.densityDpi
        val px = dp * (densityOfScreen / DisplayMetrics.DENSITY_DEFAULT)

        return px
    }


    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    @JvmStatic
    fun convertPixelsToDp(px: Float, context:Context):Float{
        val res = context.resources
        val metrics = res.displayMetrics
        val dp = px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        return dp
    }

    @JvmStatic
    fun vTotalScrollRange(appBarLayout: AppBarLayout) :Int{
        return appBarLayout.totalScrollRange
    }


}

/*
    private MathUtils() { }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }


    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static int vTotalScrollRange(AppBarLayout appBarLayout) {
        return appBarLayout.getTotalScrollRange();
    }
*/
