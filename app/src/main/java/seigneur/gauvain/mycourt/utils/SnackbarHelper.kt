package seigneur.gauvain.mycourt.utils

import android.content.Context
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.ViewCompat
import android.view.ViewGroup

import seigneur.gauvain.mycourt.R

//from :https://medium.com/@Tgo1014/creating-googles-new-snackbar-b0fe8db6c0eb
object SnackbarHelper {

    fun configSnackbar(context: Context, snack: Snackbar) {
        addMargins(snack)
        setRoundBordersBg(context, snack)
        ViewCompat.setElevation(snack.view, 6f)
    }

    private fun addMargins(snack: Snackbar) {
        val params = snack.view.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(12, 12, 12, 12)
        snack.view.layoutParams = params
    }

    private fun setRoundBordersBg(context: Context, snackbar: Snackbar) {
        snackbar.view.background = context.getDrawable(R.drawable.bg_snackbar)
    }
}