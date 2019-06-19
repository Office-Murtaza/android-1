package com.app.belcobtm.mvp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

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
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun showError(stringResId: Int) {
        runOnUiThread {
            Toast.makeText(this, stringResId, Toast.LENGTH_LONG).show()
        }
    }

    override fun showMessage(srtResId: Int) {
        runOnUiThread {
            Toast.makeText(this, srtResId, Toast.LENGTH_LONG).show()
        }
    }

    override fun showMessage(message: String) {
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