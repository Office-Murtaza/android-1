package com.app.belcobtm.ui.coins.balance

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.util.CoinItemDecoration
import kotlinx.android.synthetic.main.activity_balance.*
import kotlinx.android.synthetic.main.activity_create_wallet.progress


class BalanceActivity : BaseMvpActivity<BalanceContract.View, BalanceContract.Presenter>(),
    BalanceContract.View {

    private lateinit var mAdapter: CoinsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        mAdapter = CoinsAdapter(mPresenter.coinsList)
        coins_recycler.adapter = mAdapter
        coins_recycler.addItemDecoration(CoinItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_half)))
        mPresenter.requestCoins()

    }

    override fun notifyData() {
        balance.text = "$ ${mPresenter.balance}"

    }

    override fun showProgress(show: Boolean) {
        runOnUiThread {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
        }
    }


}
