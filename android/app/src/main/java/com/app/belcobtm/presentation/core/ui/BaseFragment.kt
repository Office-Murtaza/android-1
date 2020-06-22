package com.app.belcobtm.presentation.core.ui

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment : Fragment() {

    private fun showError(error: String?, duration: Int) {
        activity?.runOnUiThread {
            var _error = error
            if (_error.isNullOrEmpty()) _error = "Unknown error appeared"

            val containerView = activity?.findViewById<View>(R.id.container)
            if (containerView != null) {
                val snackbar = Snackbar.make(containerView, _error, Snackbar.LENGTH_SHORT)
                snackbar.view.setBackgroundColor(resources.getColor(R.color.error_color_material_light))
                snackbar.show()
            } else {
                view?.context?.let {
                    if (duration == Snackbar.LENGTH_SHORT) {
                        AlertHelper.showToastShort(it, _error)
                    } else {
                        AlertHelper.showToastLong(it, _error)
                    }
                }
            }
        }
    }

    fun showError(error: String?) {
        showError(error, Snackbar.LENGTH_SHORT)
    }

    fun showError(stringResId: Int) {
        showError(getString(stringResId), Snackbar.LENGTH_SHORT)
    }

    protected fun hideSoftKeyboard(): Boolean {
        val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        return true
    }
}