package com.belcobtm.presentation.core.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.belcobtm.R
import com.belcobtm.presentation.core.helper.AlertHelper
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity() {

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun hideSoftKeyboard(): Boolean {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

    protected fun showError(error: String?) {
        showError(error, Snackbar.LENGTH_SHORT)
    }

    protected fun showError(stringResId: Int) {
        showError(getString(stringResId), Snackbar.LENGTH_SHORT)
    }

    protected fun showError(error: String?, duration: Int) {
        runOnUiThread {
            var _error = error
            if (_error.isNullOrEmpty()) _error = "Unknown error appeared"

            val containerView = findViewById<View>(android.R.id.content)
            if (containerView != null) {
                val snackbar = Snackbar.make(containerView, _error, Snackbar.LENGTH_SHORT)
                snackbar.view.setBackgroundColor(resources.getColor(R.color.error_color_material_light))
                snackbar.show()
            } else {
                AlertHelper.showToastShort(this, _error)
            }
        }
    }

    protected fun showProgress(show: Boolean) {
        runOnUiThread {
            val progress = findViewById<FrameLayout?>(R.id.progress)
            if (progress != null) {
                val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
            }
        }
    }
}