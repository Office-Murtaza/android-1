package com.app.belcobtm.presentation.features.wallet.trade.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.FragmentTradeListBinding
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.app.belcobtm.presentation.features.wallet.trade.list.delegate.NoTradesDelegate
import com.app.belcobtm.presentation.features.wallet.trade.list.delegate.TradeItemDelegate
import com.app.belcobtm.presentation.features.wallet.trade.list.model.NoTrades
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem
import org.koin.android.viewmodel.ext.android.viewModel

class TradeListFragment : BaseFragment<FragmentTradeListBinding>() {

    companion object {
        private const val TRADE_TYPE_BUNDLE_KEY = "trade.type.key"

        fun newInstance(@TradeType tradeType: Int) =
            TradeListFragment().apply {
                arguments = Bundle().apply {
                    putInt(TRADE_TYPE_BUNDLE_KEY, tradeType)
                }
            }
    }

    override val isToolbarEnabled: Boolean
        get() = false

    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    private val viewModel: TradeListViewModel by viewModel()

    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(TradeItemDelegate())
            registerDelegate(NoTradesDelegate(viewModel::resetFilters))
        }
    }

    override fun initToolbar() {
        baseBinding.toolbarView.hide()
    }

    override fun updateActionBar() {

    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeListBinding =
        FragmentTradeListBinding.inflate(inflater, container, false)

    override fun FragmentTradeListBinding.initViews() {
        trades.adapter = adapter
        trades.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    override fun FragmentTradeListBinding.initListeners() {
        trades.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisible = (trades.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1
                if (lastVisible > viewModel.lastVisibleItem.value && lastVisible == adapter.itemCount) {
                    viewModel.loadNextPage(adapter.itemCount)
                }
            }
        })
        openFilterButton.setOnClickListener {
            navigate(R.id.filter_trade_fragment)
        }
    }

    override fun FragmentTradeListBinding.initObservers() {
        viewModel.observeTrades(requireArguments().getInt(TRADE_TYPE_BUNDLE_KEY))
            .observe(viewLifecycleOwner) {
                if (it?.isRight == true) {
                    val trades = (it as Either.Right<List<TradeItem>>).b
                    adapter.update(trades.ifEmpty { listOf<ListItem>(NoTrades()) })
                } else {
                    parentViewModel.showError((it as Either.Left<Failure>).a)
                }
            }
    }
}