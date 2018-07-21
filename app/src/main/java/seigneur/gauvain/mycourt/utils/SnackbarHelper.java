package seigneur.gauvain.mycourt.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.view.ViewGroup;

import seigneur.gauvain.mycourt.R;

//from :https://medium.com/@Tgo1014/creating-googles-new-snackbar-b0fe8db6c0eb
public class SnackbarHelper {

    public static void configSnackbar(Context context, Snackbar snack) {
        addMargins(snack);
        setRoundBordersBg(context, snack);
        ViewCompat.setElevation(snack.getView(), 6f);
    }

    private static void addMargins(Snackbar snack) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snack.getView().getLayoutParams();
        params.setMargins(12, 12, 12, 12);
        snack.getView().setLayoutParams(params);
    }

    private static void setRoundBordersBg(Context context, Snackbar snackbar) {
        snackbar.getView().setBackground(context.getDrawable(R.drawable.bg_snackbar));
    }
}