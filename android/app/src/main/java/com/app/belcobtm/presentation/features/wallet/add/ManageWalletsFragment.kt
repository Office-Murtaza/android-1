package com.app.belcobtm.presentation.features.wallet.add

import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinsAdapter
import kotlinx.android.synthetic.main.fragment_manage_wallets.*
import org.koin.android.ext.android.inject

class ManageWalletsFragment : BaseFragment() {
    private val viewModel: AddWalletViewModel by inject()
    private val adapter: AddWalletCoinsAdapter = AddWalletCoinsAdapter { position, isChecked ->
        viewModel.changeCoinState(position, isChecked)
    }

    override val resourceLayout: Int = R.layout.fragment_manage_wallets
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val isMenuEnabled: Boolean = false

    override fun initObservers() {
        viewModel.coinListLiveData.observe(this, Observer { adapter.setItemList(it) })
    }

    override fun initViews() {
        setToolbarTitle(R.string.manage_wallets_screen_title)
        ContextCompat.getDrawable(requireContext(), R.drawable.bg_divider)?.let {
            val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            itemDecorator.setDrawable(it)
            coinListView.addItemDecoration(itemDecorator)
        }
        coinListView.adapter = adapter
    }
}
