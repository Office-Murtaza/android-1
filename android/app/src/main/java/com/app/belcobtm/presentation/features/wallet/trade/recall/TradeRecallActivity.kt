package com.app.belcobtm.presentation.features.wallet.trade.recall

import android.os.Bundle
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.BaseActivity
import org.koin.android.viewmodel.ext.android.viewModel

class TradeRecallActivity : BaseActivity() {
    private val viewModel: TradeRecallViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_recall)

        initListeners()
        initObservers()
        initViews()
    }

    private fun initListeners() {}
    private fun initObservers() {}
    private fun initViews() {}
}