package com.app.belcobtm.presentation.features.wallet.balance

import android.graphics.Color
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.balance.adapter.BalanceListItem
import com.app.belcobtm.presentation.features.wallet.balance.adapter.CoinsAdapter
import kotlinx.android.synthetic.main.fragment_balance.*
import org.koin.android.viewmodel.ext.android.viewModel

class WalletFragment : BaseFragment() {
    private val viewModel: WalletViewModel by viewModel()
    private val adapter: CoinsAdapter = CoinsAdapter {
        when (it) {
            is BalanceListItem.Coin -> navigate(WalletFragmentDirections.toTransactionsFragment(it.code))
            is BalanceListItem.AddButton -> navigate(WalletFragmentDirections.toManageWalletsFragment())
        }
    }
    override val resourceLayout: Int = R.layout.fragment_balance
    override val isToolbarEnabled: Boolean = false
    override val isMenuEnabled: Boolean = true
    override val isFirstShowContent: Boolean = false

    override fun onResume() {
        super.onResume()
        viewModel.updateBalanceData()
    }

    override fun initViews() {
        listView.adapter = adapter
        swipeToRefreshView.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE)
    }

    override fun initListeners() {
        swipeToRefreshView.setOnRefreshListener { viewModel.updateBalanceData() }
    }

    override fun initObservers() {
        viewModel.balanceLiveData.listen(
            success = {
                balanceView.text = getString(R.string.balance_screen_balance, it.first.toStringUsd())
                adapter.setItemList(it.second)
                swipeToRefreshView.isRefreshing = false
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showSnackBar(it.message ?: "")
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    else -> showErrorSomethingWrong()
                }
                swipeToRefreshView.isRefreshing = false
            }
        )
    }

    override fun showLoading() {
        if (adapter.itemCount <= 1) {
            super.showLoading()
        } else {
            hideKeyboard()
            view?.clearFocus()
            view?.requestFocus()
            swipeToRefreshView.isRefreshing = true
        }
    }
}
