package com.app.belcobtm.mvp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


abstract class BaseMvpFragment<in V : BaseMvpView, T : BaseMvpPresenter<V>>
    : Fragment(), BaseMvpView {

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this as V)
    }

    override fun getContext(): Context = activity!!

    @Inject
    protected lateinit var mPresenter: T


    private fun showError(error: String?, @Snackbar.Duration duration: Int) {
        activity?.runOnUiThread {
            var _error = error
            if (_error.isNullOrEmpty()) _error = "Unknown error appeared"

            val containerView = activity?.findViewById<View>(R.id.container)
            if (containerView != null) {
                val snackbar = Snackbar.make(containerView, _error, Snackbar.LENGTH_SHORT)
                snackbar.view.setBackgroundColor(resources.getColor(R.color.error_color_material_light))
                snackbar.show()
            } else {
                if (duration == Snackbar.LENGTH_SHORT) {
                    AlertHelper.showToastShort(context, _error)
                } else {
                    AlertHelper.showToastLong(context, _error)
                }
            }
        }
    }

    override fun showError(error: String?) {
        showError(error, Snackbar.LENGTH_SHORT)
    }

    override fun showError(stringResId: Int) {
        showError(getString(stringResId), Snackbar.LENGTH_SHORT)
    }

    override fun showLongError(error: String?) {
        showError(error, Snackbar.LENGTH_LONG)
    }

    override fun showLongError(stringResId: Int) {
        showError(getString(stringResId), Snackbar.LENGTH_LONG)
    }

    override fun showMessage(srtResId: Int) {
        activity?.runOnUiThread {
            AlertHelper.showToastLong(context, srtResId)
        }
    }

    override fun showMessage(message: String?) {
        activity?.runOnUiThread {
            AlertHelper.showToastLong(context, message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    protected fun hideSoftKeyboard(): Boolean {
        val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        return true
    }

    override fun showProgress(show: Boolean) {
        activity?.runOnUiThread {
            val progress = view?.findViewById<FrameLayout?>(R.id.progress)
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

    override fun onRefreshTokenFailed() {
        if (isVisible)
            startActivity(Intent(context, PinActivity::class.java))
    }
}