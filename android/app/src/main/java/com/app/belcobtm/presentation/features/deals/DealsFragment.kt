package com.app.belcobtm.presentation.features.deals

import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_deals.*

class DealsFragment : BaseFragment() {
    override val resourceLayout: Int = R.layout.fragment_deals
    override var isMenuEnabled: Boolean = true

    override fun initViews() {
        setToolbarTitle(R.string.deals_toolsbar_title)
    }

    override fun initListeners() {
        swapItem.setOnClickListener { navigate(DealsFragmentDirections.toSwapFragment()) }
        stakingItem.setOnClickListener { navigate(DealsFragmentDirections.toStakingFragment()) }
    }
}
