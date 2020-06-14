package com.app.belcobtm.presentation.features.wallet.balance

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseFragment
import com.app.belcobtm.presentation.features.authorization.pin.PinActivity
import com.app.belcobtm.presentation.features.wallet.add.AddWalletActivity
import com.app.belcobtm.presentation.features.wallet.balance.adapter.BalanceListItem
import com.app.belcobtm.presentation.features.wallet.balance.adapter.CoinsAdapter
import com.app.belcobtm.presentation.features.wallet.transactions.TransactionsActivity
import kotlinx.android.synthetic.main.fragment_balance.*
import org.koin.android.viewmodel.ext.android.viewModel

class BalanceFragment : BaseFragment() {
    private val viewModel: BalanceViewModel by viewModel()
    private val adapter: CoinsAdapter = CoinsAdapter {
        when (it) {
            is BalanceListItem.Coin -> {
                val intent = Intent(activity, TransactionsActivity::class.java)
                intent.putExtra(TransactionsActivity.KEY_COIN_CODE, it.code)
                startActivity(intent)
            }
            is BalanceListItem.AddButton -> {
                startActivity(Intent(context, AddWalletActivity::class.java))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_balance, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initListeners()
        initObservers()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateBalanceData()
    }

    private fun initListeners() {
        swipeToRefreshView.setOnRefreshListener { viewModel.updateBalanceData() }
    }

    private fun initObservers() {
        viewModel.balanceLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is LoadingData.Loading -> progressView.show()
                is LoadingData.Success -> {
                    balanceView.text = it.data.first.toStringUsd()
                    adapter.setItemList(it.data.second)
                    progressView.hide()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.TokenError -> startActivity(Intent(activity, PinActivity::class.java))
                        is Failure.MessageError -> showError(it.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    progressView.hide()
                }
            }
            swipeToRefreshView.isRefreshing = false
        })
    }

    private fun initViews() {
        listView.adapter = adapter
        swipeToRefreshView.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE)
    }
}
