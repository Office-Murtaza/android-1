package com.app.belcobtm.presentation.features.settings.unlink

import android.view.View
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.HostActivity
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY
import com.app.belcobtm.presentation.features.settings.phone.PhoneChangeFragmentDirections
import kotlinx.android.synthetic.main.fragment_unlink.*
import org.koin.android.viewmodel.ext.android.viewModel


class UnlinkFragment : BaseFragment() {
    val viewModel by viewModel<UnlinkViewModel>()

    override val resourceLayout = R.layout.fragment_unlink

    override fun initViews() {
        setToolbarTitle(R.string.unlink_wallet_label)
    }

    override fun initListeners() {
        nextButton.setOnClickListener {
            viewModel.unlink()
        }
    }

    override fun popBackStack(): Boolean {
        getNavController()?.navigate(UnlinkFragmentDirections.unlinkToSettingsFragment(SETTINGS_SECURITY))
        return true
    }

    override fun initObservers() {
        viewModel.actionData.observe(this) { action ->
            when (action) {
                is UnlinkAction.Loading -> showLoading()
                is UnlinkAction.Failure -> {
                    showContent()
                    showError(R.string.error_something_went_wrong)
                }
                is UnlinkAction.Success -> {
                    (requireActivity() as? HostActivity)?.showAuthorizationScreen()
                }
            }
        }
    }
}