package seigneur.gauvain.mycourt.utils

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import android.util.AttributeSet
import android.view.Gravity
import android.view.View

class BottomNavigationBehavior<V : View>(context: Context, attrs: AttributeSet) :
        androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<V>(context, attrs) {

    // Rest of the code is the same
    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: V, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateSnackbar(child, dependency)
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    private fun updateSnackbar(child: View, snackbarLayout: Snackbar.SnackbarLayout) {
        if (snackbarLayout.layoutParams is androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) {
            val params = snackbarLayout.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams

            params.anchorId = child.id
            params.anchorGravity = Gravity.TOP
            params.gravity = Gravity.TOP
            snackbarLayout.layoutParams = params
        }
    }
}