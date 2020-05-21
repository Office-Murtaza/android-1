package com.app.belcobtm.presentation.features.wallet.trade.details

import android.os.Bundle
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.BaseActivity
import org.koin.android.viewmodel.ext.android.viewModel

class TradeDetailsActivity : BaseActivity() {
    private val viewModel: TradeDetailsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_details)
    }
}