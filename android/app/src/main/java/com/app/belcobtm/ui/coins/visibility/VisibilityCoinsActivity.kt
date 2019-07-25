package com.app.belcobtm.ui.coins.visibility

import android.os.Bundle
import android.view.MenuItem
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.util.CoinItemDecoration
import kotlinx.android.synthetic.main.activity_visibility_coins.*


class VisibilityCoinsActivity : BaseMvpActivity<VisibilityCoinsContract.View, VisibilityCoinsContract.Presenter>(),
    VisibilityCoinsContract.View {

    private lateinit var mAdapter: VisibilityCoinsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visibility_coins)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAdapter = VisibilityCoinsAdapter(mPresenter.coinsList, mPresenter)
        coins_recycler.adapter = mAdapter
        coins_recycler.addItemDecoration(CoinItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_half)))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun notifyList() {
        mAdapter.notifyDataSetChanged()
    }
}
