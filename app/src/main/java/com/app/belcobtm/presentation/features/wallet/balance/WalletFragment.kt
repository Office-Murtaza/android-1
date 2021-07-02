package com.app.belcobtm.presentation.features.wallet.balance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentBalanceBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.balance.adapter.CoinsAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
class WalletFragment : BaseFragment<FragmentBalanceBinding>() {

    private val viewModel: WalletViewModel by viewModel()
    private lateinit var adapter: CoinsAdapter
    override val isToolbarEnabled: Boolean = false
    override var isMenuEnabled: Boolean = true
    override val isFirstShowContent: Boolean = false
    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.reconnectToWallet() }

    override fun FragmentBalanceBinding.initViews() {
        adapter = CoinsAdapter {
            navigate(WalletFragmentDirections.toTransactionsFragment(it.code))
        }
        listView.adapter = adapter
    }

    override fun FragmentBalanceBinding.initObservers() {
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

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentBalanceBinding =
        FragmentBalanceBinding.inflate(inflater, container, false)
}
