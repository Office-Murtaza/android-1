package com.app.belcobtm.presentation.features.settings.unlink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentUnlinkBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.HostActivity
import org.koin.android.viewmodel.ext.android.viewModel


class UnlinkFragment : BaseFragment<FragmentUnlinkBinding>() {
    val viewModel by viewModel<UnlinkViewModel>()
    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true
    override val retryListener = View.OnClickListener {
        viewModel.unlink()
    }

    override fun FragmentUnlinkBinding.initViews() {
        setToolbarTitle(R.string.unlink_wallet_label)
    }

    override fun FragmentUnlinkBinding.initListeners() {
        nextButton.setOnClickListener {
            viewModel.unlink()
        }
    }

    override fun FragmentUnlinkBinding.initObservers() {
        viewModel.actionData.listen(
            success = {
                showSnackBar(R.string.wallet_unlinked)
                (requireActivity() as? HostActivity)?.showAuthorizationScreen()
            }
        )
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentUnlinkBinding =
        FragmentUnlinkBinding.inflate(inflater, container, false)
}