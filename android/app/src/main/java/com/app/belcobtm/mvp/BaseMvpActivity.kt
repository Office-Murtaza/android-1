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
import androidx.appcompat.app.AppCompatActivity
import com.app.belcobtm.R
import com.app.belcobtm.ui.auth.pin.PinActivity
import com.app.belcobtm.ui.auth.welcome.WelcomeActivity
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import javax.inject.Inject

abstract class BaseMvpActivity<in V : BaseMvpView, T : BaseMvpPresenter<V>>
    : AppCompatActivity(), BaseMvpView {

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this as V)
    }

    @Inject
    protected lateinit var mPresenter: T

    override fun getContext(): Context = this

    override fun showError(error: String?) {
        runOnUiThread {
            val containerView = findViewById<View>(R.id.container)
            val snackbar = Snackbar.make(containerView, error!!, Snackbar.LENGTH_SHORT)
            snackbar.view.setBackgroundColor(getColor(R.color.error_color_material_light))
            snackbar.show()
        }
    }

    override fun showError(stringResId: Int) {
        runOnUiThread {
            val containerView = findViewById<View>(R.id.container)
            val snackbar = Snackbar.make(containerView, stringResId, Snackbar.LENGTH_SHORT)
            snackbar.view.setBackgroundColor(getColor(R.color.error_color_material_light))
            snackbar.show()
        }
    }

    override fun showMessage(srtResId: Int) {
        runOnUiThread {
            Toast.makeText(this, srtResId, Toast.LENGTH_LONG).show()
        }
    }

    override fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun showProgress(show: Boolean) {
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

    override fun onRefreshTokenFailed() {
        startActivity(Intent(this, PinActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    protected fun hideSoftKeyboard(): Boolean {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }
}