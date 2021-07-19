package com.belcobtm.presentation.features.wallet.trade.mytrade.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.belcobtm.R
import com.belcobtm.databinding.FragmentMyTradesBinding
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.core.extensions.hide
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.wallet.trade.container.TradeContainerFragmentDirections
import com.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.features.wallet.trade.mytrade.list.delegate.MyTradeDelegate
import com.belcobtm.presentation.features.wallet.trade.mytrade.list.delegate.MyTradesNoTradesDelegate
import org.koin.android.viewmodel.ext.android.viewModel

class MyTradesFragment : BaseFragment<FragmentMyTradesBinding>() {

    override val isToolbarEnabled: Boolean
        get() = false

    override val isHomeButtonEnabled: Boolean
        get() = false

    override var isMenuEnabled: Boolean = false

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(MyTradeDelegate {
                navigate(TradeContainerFragmentDirections.toMyTradeDetails(it.tradeId))
            })
            registerDelegate(MyTradesNoTradesDelegate {
                navigate(R.id.create_trade_fragment)
            })
        }
    }

    override fun initToolbar() {
        baseBinding.toolbarView.hide()
    }

    override fun updateActionBar() {

    }

    private val viewModel by viewModel<MyTradesViewModel>()

    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyTradesBinding =
        FragmentMyTradesBinding.inflate(inflater, container, false)

    override fun FragmentMyTradesBinding.initViews() {
        myTradesList.adapter = adapter
        myTradesList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    override fun FragmentMyTradesBinding.initObservers() {
        viewModel.observeMyTrades().observe(viewLifecycleOwner) {
            if (it?.isRight == true) {
                adapter.update((it as Either.Right<List<ListItem>>).b)
            } else {
                parentViewModel.showError((it as Either.Left<Failure>).a)
            }
        }
    }
}