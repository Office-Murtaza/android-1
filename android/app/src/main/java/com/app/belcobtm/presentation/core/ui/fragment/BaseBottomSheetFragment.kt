package com.app.belcobtm.presentation.core.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.databinding.IncludeErrorScreenBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.invisible
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseBottomSheetFragment : BottomSheetDialogFragment() {

    protected abstract val errorBinding: IncludeErrorScreenBinding
    protected abstract val progressView: View
    protected abstract val contentView: View
    protected open val retryListener: View.OnClickListener? = null

    protected val baseErrorHandler: (error: Failure?) -> Unit = { error ->
        when (error) {
            is Failure.NetworkConnection -> showErrorNoInternetConnection()
            is Failure.MessageError -> {
                showSnackBar(error.message ?: "")
                showContent()
            }
            is Failure.ValidationError -> {
                showError(error.message ?: "")
                showContent()
            }
            is Failure.XRPLowAmountToSend -> {
                showError(R.string.error_xrp_amount_is_not_enough)
                showContent()
            }
            is Failure.ServerError -> showErrorServerError()
            else -> showErrorSomethingWrong()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        errorBinding.errorRetryButtonView.setOnClickListener(retryListener)
    }

    protected fun showSnackBar(resMessage: Int) = Snackbar.make(
        requireActivity().findViewById<ViewGroup>(android.R.id.content),
        resMessage,
        Snackbar.LENGTH_SHORT
    ).show()

    protected fun showSnackBar(message: String?): Unit = Snackbar.make(
        requireActivity().findViewById<ViewGroup>(android.R.id.content),
        message ?: "",
        Snackbar.LENGTH_SHORT
    ).show()

    protected open fun showContent() {
        updateContentContainer(isContentVisible = true)
    }

    protected open fun showError(resMessage: Int) {
        contentView.show()
        progressView.hide()
        showSnackBar(resMessage)
    }

    protected open fun showError(message: String) {
        contentView.show()
        progressView.hide()
        showSnackBar(message)
    }

    protected fun <T> LiveData<LoadingData<T>>.listen(
        success: (data: T) -> Unit = {},
        error: (error: Failure?) -> Unit = baseErrorHandler,
        onUpdate: ((LoadingData<T>) -> Unit)? = null
    ) {
        this.observe(viewLifecycleOwner, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<T> -> showLoading()
                is LoadingData.Success<T> -> {
                    success.invoke(loadingData.data)
                    showContent()
                }
                is LoadingData.Error<T> -> error.invoke(loadingData.errorType)
            }
            onUpdate?.invoke(loadingData)
        })
    }

    protected open fun showLoading() {
        hideKeyboard()
        view?.clearFocus()
        view?.requestFocus()
        updateContentContainer(isProgressVisible = true)
    }

    protected fun hideKeyboard() = activity?.currentFocus?.let { focus ->
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(focus.windowToken, 0)
    }

    protected open fun showErrorNoInternetConnection() {
        errorBinding.errorImageView.setImageResource(R.drawable.ic_screen_state_no_internet)
        errorBinding.errorTitleView.setText(R.string.base_screen_no_internet_title)
        errorBinding.errorDescriptionView.setText(R.string.base_screen_no_internet_description)
        updateContentContainer(isErrorVisible = true)
    }

    protected open fun showErrorServerError() {
        errorBinding.errorImageView.setImageResource(R.drawable.ic_screen_state_server_error)
        errorBinding.errorTitleView.setText(R.string.base_screen_server_error_title)
        errorBinding.errorDescriptionView.setText(R.string.base_screen_server_error_description)
        updateContentContainer(isErrorVisible = true)
    }

    protected open fun showErrorSomethingWrong() {
        errorBinding.errorImageView.setImageResource(R.drawable.ic_screen_state_something_wrong)
        errorBinding.errorTitleView.setText(R.string.base_screen_something_wrong_title)
        errorBinding.errorDescriptionView.setText(R.string.base_screen_something_wrong_description)
        updateContentContainer(isErrorVisible = true)
    }

    private fun updateContentContainer(
        isContentVisible: Boolean = false,
        isProgressVisible: Boolean = false,
        isErrorVisible: Boolean = false
    ) {
        if (isContentVisible) contentView.toggle(true) else contentView.invisible()
        progressView.toggle(isProgressVisible)
        errorBinding.errorContainerView.toggle(isErrorVisible)
    }
}