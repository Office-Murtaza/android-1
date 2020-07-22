package com.app.belcobtm.presentation.core.ui.fragment

import android.content.ComponentCallbacks
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.HostActivity
import com.app.belcobtm.presentation.features.HostNavigationFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_base.*
import kotlinx.android.synthetic.main.fragment_base.view.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

abstract class BaseFragment : Fragment() {
    private var cachedToolbarTitle: String = ""
    private var navController: NavController? = null
    protected open val isToolbarEnabled: Boolean = true
    protected open val isHomeButtonEnabled: Boolean = false
    protected open val isMenuEnabled: Boolean = false
    protected open val homeButtonDrawable: Int = R.drawable.ic_arrow_back
    protected open val backPressedListener: View.OnClickListener = View.OnClickListener { popBackStack() }

    protected abstract val resourceLayout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(isToolbarEnabled)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressedListener.onClick(null)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_base, container, false)
        inflater.inflate(resourceLayout, rootView.findViewById<FrameLayout>(R.id.contentContainerView), true)
        (activity as AppCompatActivity).setSupportActionBar(rootView.toolbarView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.navController = Navigation.findNavController(view)
        initListeners()
        initObservers()
        initViews()
        showContent()
    }

    override fun onResume() {
        super.onResume()
        activity?.supportFragmentManager?.findFragmentByTag(HostNavigationFragment::class.java.name)?.let {
            if (isMenuEnabled) {
                (it as HostNavigationFragment).showBottomMenu()
            } else {
                (it as HostNavigationFragment).hideBottomMenu()
            }
        }

        val activity = activity as AppCompatActivity
        activity.supportActionBar?.let { actionBar ->
            if (isToolbarEnabled) {
                val drawable = ContextCompat.getDrawable(activity.applicationContext, homeButtonDrawable)
                drawable?.setTint(ContextCompat.getColor(activity.applicationContext, R.color.colorPrimary))
                (activity as HostActivity).supportActionBar?.setHomeAsUpIndicator(drawable)
                actionBar.show()
            } else {
                actionBar.hide()
            }

            actionBar.setDisplayShowHomeEnabled(isHomeButtonEnabled && isToolbarEnabled)
            actionBar.setDisplayHomeAsUpEnabled(isHomeButtonEnabled && isToolbarEnabled)
            fillToolbarTitle()
        }
        activity.invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = if (item.itemId == android.R.id.home) {
        hideKeyboard()
        popBackStack()
        true
    } else {
        false
    }

    protected inline fun <reified T : Any> ComponentCallbacks.injectPresenter() = lazy {
        get<T>(null) { parametersOf(this) }
    }

    protected fun getNavController(): NavController? = navController

    protected fun navigate(resId: Int) {
        navController?.navigate(resId)
    }

    protected fun navigate(resId: Int, args: Bundle) {
        navController?.navigate(resId, args)
    }

    protected fun navigate(resId: Int, args: Bundle, extras: Navigator.Extras) {
        navController?.navigate(resId, args, null, extras)
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

    protected fun showSnackBar(resMessage: Int) = Snackbar.make(
        requireActivity().findViewById<ViewGroup>(android.R.id.content),
        resMessage,
        Snackbar.LENGTH_SHORT
    ).also { it.view.setBackgroundColor(ContextCompat.getColor(it.view.context, R.color.colorErrorSnackBar)) }.show()

    protected fun showSnackBar(message: String?): Unit = Snackbar.make(
        requireActivity().findViewById<ViewGroup>(android.R.id.content),
        message ?: "",
        Snackbar.LENGTH_SHORT
    ).also { it.view.setBackgroundColor(ContextCompat.getColor(it.view.context, R.color.colorErrorSnackBar)) }.show()

    private fun fillToolbarTitle() = (activity as? HostActivity)?.let {
        it.supportActionBar?.title = if (cachedToolbarTitle.isBlank()) "" else cachedToolbarTitle
    }

    protected fun hideKeyboard() = activity?.currentFocus?.let { focus ->
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(focus.windowToken, 0)
    }

    protected open fun initViews() = Unit

    protected open fun initListeners() = Unit

    protected open fun initObservers() = Unit

    protected open fun showLoading() {
        hideKeyboard()
        view?.clearFocus()
        view?.requestFocus()
        contentContainerView.hide()
        progressView.show()
    }

    protected open fun showContent() {
        progressView.hide()
        contentContainerView.show()
    }

    protected open fun showError(message: String) {
        contentContainerView.hide()
        progressView.hide()
        showSnackBar(message)
    }

    protected open fun showError(resMessage: Int) {
        contentContainerView.hide()
        progressView.hide()
        showSnackBar(resMessage)
    }

    protected fun <T> MutableLiveData<LoadingData<T>>.listen(
        success: (data: T) -> Unit,
        error: ((error: Failure?) -> Unit)? = null
    ) {
        this.observe(viewLifecycleOwner, Observer { loadingData ->
            when (loadingData) {
                is LoadingData.Loading<T> -> showLoading()
                is LoadingData.Success<T> -> {
                    success.invoke(loadingData.data)
                    showContent()
                }
                is LoadingData.Error<T> -> if (error == null) {
                    when (loadingData.errorType) {
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        is Failure.MessageError,
                        is Failure.ServerError -> showError(loadingData.errorType.message ?: "")
                        else -> showError(R.string.error_something_went_wrong)
                    }
                } else {
                    error.invoke(loadingData.errorType)
                    showContent()
                }
            }
        })
    }
}