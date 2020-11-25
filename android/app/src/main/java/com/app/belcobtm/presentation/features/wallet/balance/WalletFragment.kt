package com.app.belcobtm.presentation.features.wallet.balance

import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.balance.adapter.CoinsAdapter
import kotlinx.android.synthetic.main.fragment_balance.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
class WalletFragment : BaseFragment() {
    private val viewModel: WalletViewModel by viewModel()
    private lateinit var adapter: CoinsAdapter
    override val resourceLayout: Int = R.layout.fragment_balance
    override val isToolbarEnabled: Boolean = false
    override var isMenuEnabled: Boolean = true
    override val isFirstShowContent: Boolean = false
    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.reconnectToWallet() }

    override fun initViews() {
        adapter = CoinsAdapter {
            navigate(WalletFragmentDirections.toTransactionsFragment(it.code))
        }
        listView.adapter = adapter
    }

    override fun initObservers() {
        viewModel.balanceLiveData.listen(
            success = {
                balanceView.text = getString(R.string.text_usd, it.first.toStringUsd())
                adapter.setItemList(it.second)
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
            }
        )
    }
}
