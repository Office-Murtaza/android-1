package com.app.belcobtm.mvp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.belcobtm.R
import com.google.android.material.snackbar.Snackbar

abstract class BaseMvpActivity<in V : BaseMvpView, T : BaseMvpPresenter<V>>
    : AppCompatActivity(), BaseMvpView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        injectDependency()
        mPresenter.attachView(this as V)
    }

//    protected abstract fun injectDependency()

    override fun getContext(): Context = this

    protected abstract var mPresenter: T


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