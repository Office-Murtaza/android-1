package com.app.belcobtm.presentation.core.ui.fragment

import android.content.ComponentCallbacks
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentBaseBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.views.InterceptableFrameLayout
import com.app.belcobtm.presentation.features.HostActivity
import com.app.belcobtm.presentation.features.HostNavigationFragment
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

abstract class BaseFragment<V : ViewBinding> : Fragment(),
    InterceptableFrameLayout.OnInterceptEventListener {
    private var cachedToolbarTitle: String = ""
    private var navController: NavController? = null
    protected open val isToolbarEnabled: Boolean = true
    protected open val isHomeButtonEnabled: Boolean = false
    protected open var isMenuEnabled: Boolean = false
    protected open val homeButtonDrawable: Int = R.drawable.ic_arrow_back
    protected open val retryListener: View.OnClickListener? = null
    protected open val backPressedListener: View.OnClickListener = View.OnClickListener { popBackStack() }
    protected open val isFirstShowContent: Boolean = true

    //field used for dynamic setting of back button because we handle it on resume
    protected open var isBackButtonEnabled: Boolean = false
    protected lateinit var binding: V
        private set
    protected lateinit var baseBinding: FragmentBaseBinding
        private set

    protected val baseErrorHandler: (error: Failure?) -> Unit = { error ->
        when (error) {
            is Failure.NetworkConnection -> showErrorNoInternetConnection()
            is Failure.MessageError -> {
                showSnackBar(error.message ?: "")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressedListener.onClick(null)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return baseBinding.root
    }

    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): V

    protected open fun initToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(baseBinding.toolbarView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.navController = findNavController()
        updateActionBar()
        baseBinding.interceptableFrameLayout.interceptListner = this
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

    override fun onDestroyView() {
        super.onDestroyView()
        this.navController = null
    }

    override fun onResume() {
        super.onResume()
        showBottomMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            hideKeyboard()
            popBackStack()
            true
        } else {
            false
        }

    override fun onIntercented(ev: MotionEvent) {
        if (ev.action == MotionEvent.ACTION_DOWN) hideKeyboard()
    }

    protected inline fun <reified T : Any> ComponentCallbacks.injectPresenter() = lazy {
        get<T>(null) { parametersOf(this) }
    }

    protected fun getNavController(): NavController? = navController

    //catch exceptions in this block for simultaneous tap and navigation issue based on destination change
    protected fun navigate(resId: Int) {
        try {
            navController?.navigate(resId)
        } catch (e: Exception) {
            //catch exception for navigation
        }
    }

    protected fun navigate(resId: Int, args: Bundle) {
        try {
            navController?.navigate(resId, args)
        } catch (e: Exception) {
            //catch exception for navigation
        }
    }

    protected fun navigate(resId: Int, args: Bundle, extras: Navigator.Extras) {
        try {
            navController?.navigate(resId, args, null, extras)
        } catch (e: Exception) {
            //catch exception for navigation
        }
    }

    protected fun navigate(navDestination: NavDirections) {
        try {
            navController?.navigate(navDestination)
        } catch (e: Exception) {
            e.printStackTrace()
            //catch exception for navigation
        }
    }

    protected fun setGraph(graphResId: Int) {
        navController?.setGraph(graphResId)
    }

    open fun popBackStack() = navController?.popBackStack() ?: false

    protected fun popBackStack(destinationId: Int, inclusive: Boolean): Boolean {
        return navController?.popBackStack(destinationId, inclusive) ?: false
    }

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
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(show)
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

    private fun fillToolbarTitle() = (activity as? HostActivity)?.let {
        it.supportActionBar?.title = if (cachedToolbarTitle.isBlank()) "" else cachedToolbarTitle
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
        showSnackBar(message)
    }

    protected open fun showError(resMessage: Int) {
        baseBinding.contentContainerView.show()
        baseBinding.progressView.hide()
        showSnackBar(resMessage)
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

    protected fun showBottomMenu() {
        activity?.supportFragmentManager?.findFragmentByTag(HostNavigationFragment::class.java.name)
            ?.let {
                if (isMenuEnabled) {
                    (it as HostNavigationFragment).showBottomMenu()
                } else {
                    (it as HostNavigationFragment).hideBottomMenu()
                }
            }
    }

    private fun updateContentContainer(
        isContentVisible: Boolean = false,
        isProgressVisible: Boolean = false,
        isErrorVisible: Boolean = false
    ) {
        baseBinding.contentContainerView.toggle(isContentVisible)
        baseBinding.progressView.toggle(isProgressVisible)
        baseBinding.errorView.errorContainerView.toggle(isErrorVisible)
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
                is LoadingData.Error<T> -> {
                    hideKeyboard()
                    error.invoke(loadingData.errorType)
                }
            }
            onUpdate?.invoke(loadingData)
        })
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
                val drawable = ContextCompat.getDrawable(activity.applicationContext, homeButtonDrawable)
                drawable?.setTint(ContextCompat.getColor(activity.applicationContext, R.color.colorPrimary))
                (activity as HostActivity).supportActionBar?.setHomeAsUpIndicator(drawable)
                actionBar.show()
            } else {
                actionBar.hide()
            }

            showBackButton(isBackButtonEnabled || (isToolbarEnabled && isHomeButtonEnabled))
            fillToolbarTitle()
        }
        activity.invalidateOptionsMenu()
    }
}