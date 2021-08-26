package com.belcobtm.presentation.features.authorization.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.belcobtm.R
import com.belcobtm.databinding.FragmentWelcomeBinding
import com.belcobtm.presentation.core.ui.fragment.BaseFragment

class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    override val isToolbarEnabled: Boolean = false

    override fun FragmentWelcomeBinding.initViews() {
        pagerView.apply {
            adapter = WelcomePagerAdapter()
            adapter?.registerAdapterDataObserver(pagerIndicatorView.adapterDataObserver)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        pagerIndicatorView.setViewPager(pagerView)
    }

    override fun FragmentWelcomeBinding.initListeners() {
        contactSupportButtonView.setOnClickListener { startSupportFragment() }
        createNewWalletButtonView.setOnClickListener { navigate(R.id.to_create_wallet_fragment) }
        recoverWalletButtonView.setOnClickListener { navigate(R.id.to_recover_wallet_fragment) }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWelcomeBinding =
        FragmentWelcomeBinding.inflate(inflater, container, false)

    private fun startSupportFragment() {
        val direction = WelcomeFragmentDirections.toSupportFragment()
        findNavController().navigate(direction)
    }
}
