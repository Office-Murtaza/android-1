package com.app.belcobtm.ui.main.coins.balance

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.mvp.BaseMvpFragment
import com.app.belcobtm.ui.main.coins.transactions.TransactionsActivity
import com.app.belcobtm.ui.main.coins.visibility.VisibilityCoinsActivity
import com.app.belcobtm.presentation.core.CoinItemDecoration
import kotlinx.android.synthetic.main.fragment_balance.*


class BalanceFragment : BaseMvpFragment<BalanceContract.View, BalanceContract.Presenter>(),
    BalanceContract.View,
    CoinsAdapter.OnCoinClickListener {

    private lateinit var mAdapter: CoinsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_balance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapter = CoinsAdapter(mPresenter.coinsList, this)
        coins_recycler.isNestedScrollingEnabled = false
        coins_recycler.adapter = mAdapter
        coins_recycler.addItemDecoration(CoinItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_half)))

        swipeToRefreshView.setOnRefreshListener { mPresenter.requestCoins() }
        swipeToRefreshView.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE)

        add_wallet_ll.setOnClickListener {
            startActivity(Intent(context, VisibilityCoinsActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        mPresenter.requestCoins()
    }

    override fun notifyData() {
        balance.text = "$ ${String.format("%.2f", mPresenter.balance)}"
        mAdapter.notifyDataSetChanged()
    }

    override fun showProgress(show: Boolean) {
        activity?.runOnUiThread {
            if (!show) {
                swipeToRefreshView.isRefreshing = false
            }
            super.showProgress(show)
        }
    }

    override fun onCoinClick(coin: CoinModel, coinArray: List<CoinModel>) {
        TransactionsActivity.start(activity, coin, coinArray)
    }
}
