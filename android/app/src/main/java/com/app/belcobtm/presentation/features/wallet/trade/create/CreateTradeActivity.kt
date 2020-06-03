package com.app.belcobtm.presentation.features.wallet.trade.create

import android.os.Bundle
import android.view.MenuItem
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_create_trade.*
import org.koin.android.viewmodel.ext.android.viewModel

class CreateTradeActivity : BaseActivity() {
    private val viewModel: CreateTradeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_trade)
        initListeners()
        initObservers()
        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initListeners() {
        createButtonView.setOnClickListener { }
    }

    private fun initObservers() {

    }

    private fun initViews() {

    }
}