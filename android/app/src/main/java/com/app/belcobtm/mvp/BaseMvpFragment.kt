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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.belcobtm.R
import com.app.belcobtm.ui.auth.pin.PinActivity
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


    override fun showError(error: String?) {
        activity?.runOnUiThread {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun showError(stringResId: Int) {
        activity?.runOnUiThread {
            Toast.makeText(context, stringResId, Toast.LENGTH_LONG).show()
        }
    }

    override fun showMessage(srtResId: Int) {
        activity?.runOnUiThread {
            Toast.makeText(context, srtResId, Toast.LENGTH_LONG).show()
        }
    }

    override fun showMessage(message: String?) {
        activity?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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