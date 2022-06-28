package com.belcobtm.presentation.features.settings.unlink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.databinding.FragmentUnlinkBinding
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.HostActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class UnlinkFragment : BaseFragment<FragmentUnlinkBinding>() {
    val viewModel by viewModel<UnlinkViewModel>()
    override val isBackButtonEnabled = true
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
                showToast(R.string.wallet_unlinked)
                (requireActivity() as? HostActivity)?.showAuthorizationScreen()
            }
        )
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentUnlinkBinding =
        FragmentUnlinkBinding.inflate(inflater, container, false)
}