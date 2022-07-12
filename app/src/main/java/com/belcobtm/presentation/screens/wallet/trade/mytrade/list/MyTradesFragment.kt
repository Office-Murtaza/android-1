package com.belcobtm.presentation.screens.wallet.trade.mytrade.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeMyListBinding
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.wallet.trade.container.TradeContainerFragmentDirections
import com.belcobtm.presentation.screens.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.screens.wallet.trade.mytrade.list.delegate.MyTradeDelegate
import com.belcobtm.presentation.screens.wallet.trade.mytrade.list.delegate.MyTradesLoadingDelegate
import com.belcobtm.presentation.tools.extensions.hide
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyTradesFragment : BaseFragment<FragmentTradeMyListBinding>() {

    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.retryDelete() }

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(MyTradeDelegate(viewModel::delete) {
                navigate(TradeContainerFragmentDirections.toMyTradeDetails(it.tradeId))
            })
            registerDelegate(MyTradesLoadingDelegate())
        }
    }

    private val viewModel by viewModel<MyTradesViewModel>()

    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    // to not create second toolbar in child fragment and leave the one in parent
    override fun initToolbar() {
        baseBinding.toolbarView.hide()
    }

    override fun updateActionBar() {
        // to not create second toolbar in child fragment and leave the one in parent
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTradeMyListBinding = FragmentTradeMyListBinding.inflate(inflater, container, false)

    override fun FragmentTradeMyListBinding.initViews() {
        tradesRecyclerView.apply {
            adapter = this@MyTradesFragment.adapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            )
        }
        openCreateTradeButton.setOnClickListener {
            navigate(R.id.create_trade_fragment)
        }
    }

    override fun FragmentTradeMyListBinding.initObservers() {
        viewModel.tradesLiveData.observe(viewLifecycleOwner) {
            if (it.isRight) {
                val list = (it as Either.Right<List<ListItem>>).b
                toggleEmptyState(list.isEmpty())
                adapter.update(list)
            } else {
                parentViewModel.showError((it as Either.Left<Failure>).a)
            }
        }
        viewModel.deleteTradeLoadingData.listen()
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        binding.tradesRecyclerView.isVisible = isEmpty.not()
        binding.emptyStateLayout.isVisible = isEmpty
    }

}
