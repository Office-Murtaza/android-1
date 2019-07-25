package com.app.belcobtm.ui.coins.balance

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpFragment
import com.app.belcobtm.ui.auth.pin.PinActivity
import com.app.belcobtm.ui.auth.welcome.WelcomeActivity
import com.app.belcobtm.ui.coins.visibility.VisibilityCoinsActivity
import com.app.belcobtm.util.CoinItemDecoration
import kotlinx.android.synthetic.main.activity_balance.*


class BalanceFragment : BaseMvpFragment<BalanceContract.View, BalanceContract.Presenter>(),
    BalanceContract.View {

    private lateinit var mAdapter: CoinsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_balance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter.checkPinEntered()

        mAdapter = CoinsAdapter(mPresenter.coinsList)
        coins_recycler.isNestedScrollingEnabled = false
        coins_recycler.adapter = mAdapter
        coins_recycler.addItemDecoration(CoinItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_half)))

        swipe_refresh.setOnRefreshListener { mPresenter.requestCoins() }
        swipe_refresh.setColorSchemeColors(
            Color.RED, Color.GREEN, Color.BLUE
        )

        add_wallet.setOnClickListener { startActivity(Intent(context, VisibilityCoinsActivity::class.java)) }
    }


    override fun onStart() {
        super.onStart()
        mPresenter.requestCoins()
    }

    override fun notifyData() {
        balance.text = "$ ${mPresenter.balance}"
        mAdapter.notifyDataSetChanged()
    }

    override fun onTokenNotSaved() {
        activity?.finishAffinity()
        startActivity(Intent(context, WelcomeActivity::class.java))
    }

    override fun onPinSaved() {
        val mode = PinActivity.Companion.Mode.MODE_PIN
        val intent = PinActivity.getIntent(context, mode)
        startActivityForResult(intent, mode.ordinal)
    }

    override fun onPinNotSaved() {
        val mode = PinActivity.Companion.Mode.MODE_CREATE_PIN
        val intent = PinActivity.getIntent(context, mode)
        startActivityForResult(intent, mode.ordinal)
    }

    override fun showProgress(show: Boolean) {
        activity?.runOnUiThread {
            if (!show)
                swipe_refresh.isRefreshing = false

            super.showProgress(show)
        }
    }

}
