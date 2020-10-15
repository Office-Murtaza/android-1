package com.app.belcobtm.presentation.features.settings.unlink

import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.HostActivity
import kotlinx.android.synthetic.main.fragment_unlink.*
import org.koin.android.viewmodel.ext.android.viewModel


class UnlinkFragment : BaseFragment() {
    val viewModel by viewModel<UnlinkViewModel>()
    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true
    override val retryListener = View.OnClickListener {
        viewModel.unlink()
    }

    override val resourceLayout = R.layout.fragment_unlink

    override fun initViews() {
        setToolbarTitle(R.string.unlink_wallet_label)
    }

    override fun initListeners() {
        nextButton.setOnClickListener {
            viewModel.unlink()
        }
    }

    override fun initObservers() {
        viewModel.actionData.listen(
            success = {
                showSnackBar(R.string.wallet_unlinked)
                (requireActivity() as? HostActivity)?.showAuthorizationScreen()
            }
        )
    }
}