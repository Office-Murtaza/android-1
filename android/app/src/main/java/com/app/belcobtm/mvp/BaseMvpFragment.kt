package com.app.belcobtm.mvp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment


abstract class BaseMvpFragment<in V : BaseMvpView, T : BaseMvpPresenter<V>>
    : Fragment(), BaseMvpView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this as V)
    }

    override fun getContext(): Context = activity!!

    protected abstract var mPresenter: T


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
}