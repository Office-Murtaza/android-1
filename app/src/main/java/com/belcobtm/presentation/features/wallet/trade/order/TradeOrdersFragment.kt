package com.belcobtm.presentation.features.wallet.trade.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.belcobtm.databinding.FragmentTradeOrdersBinding
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.wallet.trade.container.TradeContainerFragmentDirections
import com.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.features.wallet.trade.list.model.NoOrders
import com.belcobtm.presentation.features.wallet.trade.list.model.OrderItem
import com.belcobtm.presentation.features.wallet.trade.order.delegate.NoOrdersDelegate
import com.belcobtm.presentation.features.wallet.trade.order.delegate.OpenOrdersDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel

class TradeOrdersFragment : BaseFragment<FragmentTradeOrdersBinding>() {

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(OpenOrdersDelegate {
                navigate(TradeContainerFragmentDirections.toOrderDetails(it.orderId))
            })
            registerDelegate(NoOrdersDelegate())
        }
    }

    override val isToolbarEnabled: Boolean
        get() = false

    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    private val viewModel by viewModel<TradeOrdersViewModel>()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeOrdersBinding =
        FragmentTradeOrdersBinding.inflate(inflater, container, false)

    override fun FragmentTradeOrdersBinding.initViews() {
        orderList.adapter = adapter
        orderList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    override fun initToolbar() {
        baseBinding.toolbarView.hide()
    }

    override fun updateActionBar() {

    }

    override fun FragmentTradeOrdersBinding.initObservers() {
        viewModel.observeOrders().observe(viewLifecycleOwner) {
            if (it?.isRight == true) {
                val orders = (it as Either.Right<List<OrderItem>>).b
                adapter.update(orders.ifEmpty { listOf<ListItem>(NoOrders()) })
            } else {
                parentViewModel.showError((it as Either.Left<Failure>).a)
            }
        }
    }
}