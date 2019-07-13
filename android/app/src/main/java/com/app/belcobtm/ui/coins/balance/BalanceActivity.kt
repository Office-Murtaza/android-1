package com.app.belcobtm.ui.coins.balance

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.auth.pin.PinActivity
import com.app.belcobtm.ui.auth.welcome.WelcomeActivity
import com.app.belcobtm.util.CoinItemDecoration
import kotlinx.android.synthetic.main.activity_balance.*


class BalanceActivity : BaseMvpActivity<BalanceContract.View, BalanceContract.Presenter>(),
    BalanceContract.View {

    private lateinit var mAdapter: CoinsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        mPresenter.checkPinEntered()

        mAdapter = CoinsAdapter(mPresenter.coinsList)
        coins_recycler.adapter = mAdapter
        coins_recycler.addItemDecoration(CoinItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_half)))

        swipe_refresh.setOnRefreshListener { mPresenter.requestCoins() }
        swipe_refresh.setColorSchemeColors(
            Color.RED, Color.GREEN, Color.BLUE
        )
    }

    override fun onStart() {
        super.onStart()
        showProgress(true)
        mPresenter.requestCoins()
    }

    override fun notifyData() {
        balance.text = "$ ${mPresenter.balance}"
        mAdapter.notifyDataSetChanged()
    }

    override fun onTokenNotSaved() {
        finishAffinity()
        startActivity(Intent(this, WelcomeActivity::class.java))
    }

    override fun onPinSaved() {
        val mode = PinActivity.Companion.Mode.MODE_PIN
        val intent = PinActivity.getIntent(this, mode)
        startActivityForResult(intent, mode.ordinal)
    }

    override fun onPinNotSaved() {
        val mode = PinActivity.Companion.Mode.MODE_CREATE_PIN
        val intent = PinActivity.getIntent(this, mode)
        startActivityForResult(intent, mode.ordinal)
    }

    override fun showProgress(show: Boolean) {
        runOnUiThread {
            if (!show)
                swipe_refresh.isRefreshing = false

            super.showProgress(show)
        }
    }

}
