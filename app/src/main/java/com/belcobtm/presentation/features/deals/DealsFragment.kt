package com.belcobtm.presentation.features.deals

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.databinding.FragmentDealsBinding
import com.belcobtm.presentation.core.ui.fragment.BaseFragment

class DealsFragment : BaseFragment<FragmentDealsBinding>() {
    override var isMenuEnabled: Boolean = true

    override fun FragmentDealsBinding.initViews() {
        setToolbarTitle(R.string.deals_toolsbar_title)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDealsBinding =
        FragmentDealsBinding.inflate(inflater, container, false)

    override fun FragmentDealsBinding.initListeners() {
        swapItem.setOnClickListener { navigate(DealsFragmentDirections.toSwapFragment()) }
        stakingItem.setOnClickListener { navigate(DealsFragmentDirections.toStakingFragment()) }
        transferItem.setOnClickListener { navigate(DealsFragmentDirections.toContactListFragment()) }
        tradeItem.setOnClickListener { navigate(DealsFragmentDirections.toTradeContainerFragment()) }
    }
}