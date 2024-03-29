package com.belcobtm.presentation.screens.wallet.trade.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.belcobtm.databinding.FragmentTradeListBinding
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.wallet.trade.container.TradeContainerFragmentDirections
import com.belcobtm.presentation.screens.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.screens.wallet.trade.list.delegate.TradeItemDelegate
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.tools.extensions.hide
import org.koin.androidx.viewmodel.ext.android.viewModel

class TradeListFragment : BaseFragment<FragmentTradeListBinding>() {

    companion object {

        private const val TRADE_TYPE_BUNDLE_KEY = "trade.type.key"

        fun newInstance(tradeType: TradeType) =
            TradeListFragment().apply {
                arguments = Bundle().apply {
                    putString(TRADE_TYPE_BUNDLE_KEY, tradeType.name)
                }
            }
    }

    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    private val viewModel: TradeListViewModel by viewModel()

    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(TradeItemDelegate {
                navigate(TradeContainerFragmentDirections.toTradeDetails(it.tradeId))
            })
        }
    }

    // to not create second toolbar in child fragment and leave the one in parent
    override fun initToolbar() {
        baseBinding.toolbarView.hide()
    }

    override fun updateActionBar() {
        // to not create second toolbar in child fragment and leave the one in parent
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
            navigate(TradeContainerFragmentDirections.toFilterTradeFragment())
        }
    }

    override fun FragmentTradeListBinding.initObservers() {
        viewModel.observeTrades(
            TradeType.valueOf(
                requireArguments().getString(TRADE_TYPE_BUNDLE_KEY).orEmpty()
            )
        )
            .observe(viewLifecycleOwner) {
                if (it == null) {
                    return@observe
                }
                if (it.isRight) {
                    val trades = (it as Either.Right<List<TradeItem>>).b
                    adapter.update(trades)
                } else {
                    parentViewModel.showError((it as Either.Left<Failure>).a)
                }
            }
    }

}
