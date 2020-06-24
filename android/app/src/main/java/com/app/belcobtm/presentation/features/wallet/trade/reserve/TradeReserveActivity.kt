package com.app.belcobtm.presentation.features.wallet.trade.reserve

import android.os.Bundle
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.BaseActivity
import org.koin.android.viewmodel.ext.android.viewModel

class TradeReserveActivity : BaseActivity() {
    private val viewModel: TradeReserveViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_reserve)
        initListeners()
        initObservers()
        initViews()
    }

    private fun initListeners() {}
    private fun initObservers() {}
    private fun initViews() {}
}