package com.belcobtm.presentation.core.ui.fragment

import android.content.ComponentCallbacks
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.belcobtm.R
import com.belcobtm.databinding.FragmentBaseBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.tools.extensions.toggle
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.views.InterceptableFrameLayout
import com.belcobtm.presentation.features.HostActivity
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

abstract class BaseFragment<V : ViewBinding> : Fragment(),
    InterceptableFrameLayout.OnInterceptEventListener {

    private var cachedToolbarTitle: String = ""
    protected open val isToolbarEnabled: Boolean = true
    protected open var isMenuEnabled: Boolean = false
    protected open val isBackButtonEnabled: Boolean = false

    protected open val homeButtonDrawable: Int = R.drawable.ic_arrow_back
    protected open val retryListener: View.OnClickListener? = null
    protected open val isFirstShowContent: Boolean = true

    protected lateinit var binding: V
        private set
    protected lateinit var baseBinding: FragmentBaseBinding
        private set

    protected val baseErrorHandler: (error: Failure?) -> Unit = { error ->
        when (error) {
            is Failure.NetworkConnection -> showErrorNoInternetConnection()
            is Failure.MessageError -> {
                showToast(error.message ?: "")
                showContent()
            }
            is Failure.ServerError -> showErrorServerError()
            else -> showErrorSomethingWrong()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(isToolbarEnabled)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        baseBinding = FragmentBaseBinding.inflate(inflater, container, false)
        binding = createBinding(inflater, container)
        baseBinding.contentContainerView.addView(binding.root)
        initToolbar()
        return baseBinding.root
    }

    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): V

    protected open fun initToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(baseBinding.toolbarView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateActionBar()
        baseBinding.interceptableFrameLayout.interceptListener = this
        baseBinding.errorView.errorRetryButtonView.setOnClickListener(retryListener)
        with(binding) {
            initViews()
            initListeners()
            initObservers()
        }
        if (isFirstShowContent) {
            showContent()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            hideKeyboard()
            findNavController().popBackStack()
            true
        } else {
            false
        }

    override fun onTouchIntercented(ev: MotionEvent) {
        // On each MotionEvent.ACTION_UP we want to hide the keyboard
        // because the event will be triggered after any clickable element process the event
        if (ev.action == MotionEvent.ACTION_UP) {
            val currentFocusedView = activity?.currentFocus
            if (currentFocusedView is EditText) {
                // in case if current focus is on EditText
                // we want to make sure that the click was performed
                // outside the given EditText, otherwise - do nothing
                val rect = Rect()
                currentFocusedView.getGlobalVisibleRect(rect)
                // check if current focus does not contains the clicked coordinates
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    hideKeyboard()
                }
            } else {
                hideKeyboard()
            }
        }
    }

    protected inline fun <reified T : Any> ComponentCallbacks.injectPresenter() = lazy {
        get<T>(null) { parametersOf(this) }
    }

    //catch exceptions in this block for simultaneous tap and navigation issue based on destination change
    protected fun navigate(resId: Int) {
        findNavController().navigate(resId)
    }

    protected fun navigate(resId: Int, args: Bundle) {
        findNavController().navigate(resId, args)
    }

    protected fun navigate(resId: Int, args: Bundle, extras: Navigator.Extras) {
        findNavController().navigate(resId, args, null, extras)
    }

    protected fun navigate(resId: Int, options: NavOptions) {
        findNavController().navigate(resId, null, options)
    }

    protected fun navigate(resId: Int, args: Bundle, options: NavOptions) {
        findNavController().navigate(resId, args, options)
    }

    protected fun navigate(navDestination: NavDirections) {
        findNavController().navigate(navDestination)
    }

    open fun popBackStack() = findNavController().popBackStack()

    protected fun popBackStack(destinationId: Int, inclusive: Boolean): Boolean =
        findNavController().popBackStack(destinationId, inclusive)

    protected fun setToolbarTitle(title: String) {
        this.cachedToolbarTitle = title
        fillToolbarTitle()
    }

    protected fun setToolbarTitle(titleRes: Int) {
        this.cachedToolbarTitle = getString(titleRes)
        fillToolbarTitle()
    }

    protected fun showBackButton(show: Boolean) {
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(show)
        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    protected fun showToast(resMessage: Int) = showToast(getString(resMessage))

    protected fun showToast(message: String?): Unit =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    private fun fillToolbarTitle() = (activity as? HostActivity)?.let {
        it.supportActionBar?.title = cachedToolbarTitle.ifBlank { "" }
    }

    protected fun hideKeyboard() = activity?.currentFocus?.let { focus ->
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(focus.windowToken, 0)
    }

    protected fun showKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    protected open fun showLoading() {
        hideKeyboard()
        view?.clearFocus()
        view?.requestFocus()
        updateContentContainer(isProgressVisible = true)
    }

    protected open fun showContent() {
        updateContentContainer(isContentVisible = true)
    }

    protected open fun showError(message: String) {
        baseBinding.contentContainerView.show()
        baseBinding.progressView.hide()
        showToast(message)
    }

    protected open fun showError(resMessage: Int) {
        baseBinding.contentContainerView.show()
        baseBinding.progressView.hide()
        showToast(resMessage)
    }

    protected open fun showErrorNoInternetConnection() {
        baseBinding.errorView.errorImageView.setImageResource(R.drawable.ic_screen_state_no_internet)
        baseBinding.errorView.errorTitleView.setText(R.string.base_screen_no_internet_title)
        baseBinding.errorView.errorDescriptionView.setText(R.string.base_screen_no_internet_description)
        updateContentContainer(isErrorVisible = true)
    }

    protected open fun showErrorServerError() {
        baseBinding.errorView.errorImageView.setImageResource(R.drawable.ic_screen_state_server_error)
        baseBinding.errorView.errorTitleView.setText(R.string.base_screen_server_error_title)
        baseBinding.errorView.errorDescriptionView.setText(R.string.base_screen_server_error_description)
        updateContentContainer(isErrorVisible = true)
    }

    protected open fun showErrorSomethingWrong() {
        baseBinding.errorView.errorImageView.setImageResource(R.drawable.ic_screen_state_something_wrong)
        baseBinding.errorView.errorTitleView.setText(R.string.base_screen_something_wrong_title)
        baseBinding.errorView.errorDescriptionView.setText(R.string.base_screen_something_wrong_description)
        updateContentContainer(isErrorVisible = true)
    }

    protected fun updateContentContainer(
        isContentVisible: Boolean = false,
        isProgressVisible: Boolean = false,
        isErrorVisible: Boolean = false
    ) {
        baseBinding.contentContainerView.toggle(isContentVisible)
        baseBinding.progressView.toggle(isProgressVisible)
        baseBinding.errorView.errorContainerView.toggle(isErrorVisible)
        baseBinding.errorView.errorRetryButtonView.toggle(isErrorVisible)
    }

    protected fun <T> LiveData<LoadingData<T>>.listen(
        success: (data: T) -> Unit = {},
        error: (error: Failure?) -> Unit = baseErrorHandler,
        onUpdate: ((LoadingData<T>) -> Unit)? = null
    ) {
        observe(viewLifecycleOwner) { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<T> -> showLoading()
                is LoadingData.DismissProgress<T> -> showContent()
                is LoadingData.Success<T> -> {
                    success.invoke(loadingData.data)
                    showContent()
                }
                is LoadingData.Error<T> -> {
                    hideKeyboard()
                    error.invoke(loadingData.errorType)
                }
            }
            onUpdate?.invoke(loadingData)
        }
    }

    protected open fun V.initViews() = Unit

    protected open fun V.initListeners() = Unit

    protected open fun V.initObservers() = Unit

    fun <T> T.doIfChanged(old: T?, action: (T) -> Unit) {
        if (this != old) {
            action(this)
        }
    }

    protected open fun updateActionBar() {
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.let { actionBar ->
            if (isToolbarEnabled) {
                val drawable =
                    ContextCompat.getDrawable(activity.applicationContext, homeButtonDrawable)
                drawable?.setTint(
                    ContextCompat.getColor(
                        activity.applicationContext,
                        R.color.colorPrimary
                    )
                )
                (activity as HostActivity).supportActionBar?.setHomeAsUpIndicator(drawable)
                with(requireActivity().window) {
                    statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = false
                }
                actionBar.show()
            } else {
                actionBar.hide()
                with(requireActivity().window) {
                    statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorStatusBar)
                    WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = true
                }
            }

            showBackButton(isBackButtonEnabled || isToolbarEnabled)
            fillToolbarTitle()
        }
        activity.invalidateOptionsMenu()
    }

}
