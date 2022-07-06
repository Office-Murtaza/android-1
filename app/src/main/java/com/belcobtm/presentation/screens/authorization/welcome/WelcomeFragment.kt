package com.belcobtm.presentation.screens.authorization.welcome

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.databinding.FragmentWelcomeBinding
import com.belcobtm.presentation.core.ui.fragment.BaseFragment

class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    override val isToolbarEnabled: Boolean = false

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWelcomeBinding =
        FragmentWelcomeBinding.inflate(inflater, container, false)

    override fun FragmentWelcomeBinding.initListeners() {
        createWalletButton.setOnClickListener { navigate(R.id.to_create_wallet_fragment) }
        recoverWalletButton.setOnClickListener { navigate(R.id.to_recover_wallet_fragment) }
    }

}
