package com.app.belcobtm.presentation.features.wallet.add

import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinsAdapter
import kotlinx.android.synthetic.main.activity_visibility_coins.*
import org.koin.android.ext.android.inject

class AddWalletActivity : BaseActivity() {
    private val viewModel: AddWalletViewModel by inject()
    private val adapter: AddWalletCoinsAdapter = AddWalletCoinsAdapter { position, isChecked ->
        viewModel.changeCoinState(position, isChecked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visibility_coins)
        initObservers()
        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = if (item.itemId == android.R.id.home) {
        onBackPressed()
        true
    } else {
        super.onOptionsItemSelected(item)
    }

    private fun initObservers() {
        viewModel.coinListLiveData.observe(this, Observer { adapter.setItemList(it) })
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        ContextCompat.getDrawable(applicationContext, R.drawable.divider_add_wallet)?.let {
            val itemDecorator = DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL)
            itemDecorator.setDrawable(it)
            coinListView.addItemDecoration(itemDecorator)
        }
        coinListView.adapter = adapter
    }
}
