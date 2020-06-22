package com.app.belcobtm.presentation.features.wallet.trade.edit

import android.os.Bundle
import android.view.MenuItem
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.BaseActivity
import org.koin.android.viewmodel.ext.android.viewModel

class EditTradeActivity : BaseActivity() {
    private val viewModel: EditTradeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trade)
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
//        deleteButtonView.setOnClickListener { }
//        updateButtonView.setOnClickListener { }
    }

    private fun initObservers() {

    }

    private fun initViews() {

    }
}