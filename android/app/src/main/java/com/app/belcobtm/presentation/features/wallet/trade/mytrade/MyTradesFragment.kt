package com.app.belcobtm.presentation.features.wallet.trade.mytrade

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.app.belcobtm.databinding.FragmentMyTradesBinding
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.app.belcobtm.presentation.features.wallet.trade.mytrade.delegate.MyTradeDelegate
import com.app.belcobtm.presentation.features.wallet.trade.mytrade.delegate.MyTradesNoTradesDelegate
import org.koin.android.viewmodel.ext.android.viewModel

class MyTradesFragment : BaseFragment<FragmentMyTradesBinding>() {

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(MyTradeDelegate())
            registerDelegate(MyTradesNoTradesDelegate {
                // TODO open create trade
            })
        }
    }

    private val viewModel by viewModel<MyTradesViewModel>()

    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyTradesBinding =
        FragmentMyTradesBinding.inflate(inflater, container, false)

    override fun resolveNavController(view: View): NavController =
        requireParentFragment().findNavController()

    override fun FragmentMyTradesBinding.initViews() {
        myTradesList.adapter = adapter
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