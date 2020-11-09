package com.app.belcobtm.presentation.features.authorization.welcome

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : BaseFragment() {
    override val isToolbarEnabled: Boolean = false
    override val resourceLayout: Int = R.layout.fragment_welcome
    override val backPressedListener: View.OnClickListener =
        View.OnClickListener { requireActivity().finish() }

    override fun initViews() {
        pagerView.apply {
            adapter = WelcomePagerAdapter()
            adapter?.registerAdapterDataObserver(pagerIndicatorView.adapterDataObserver)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        pagerIndicatorView.setViewPager(pagerView)
    }

    override fun initListeners() {
        contactSupportButtonView.setOnClickListener { startSupportFragment() }
        createNewWalletButtonView.setOnClickListener { navigate(R.id.to_create_wallet_fragment) }
        recoverWalletButtonView.setOnClickListener { navigate(R.id.to_recover_wallet_fragment) }
    }

    private fun startSupportFragment() {
        val direction = WelcomeFragmentDirections.toSupportFragment()
        getNavController()?.navigate(direction)
    }
}
