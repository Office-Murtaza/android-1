package com.app.belcobtm.presentation.features.wallet.trade.order.historychat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.databinding.FragmentHistoryChatBinding
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.delegate.MyMessageDelegate
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.delegate.PartnerMessageDelegate
import org.koin.android.viewmodel.ext.android.viewModel

class HistoryChatFragment : BaseFragment<FragmentHistoryChatBinding>() {

    override var isHomeButtonEnabled: Boolean = true
    private val args by navArgs<HistoryChatFragmentArgs>()
    private val viewModel by viewModel<HistoryChatViewModel>()
    private val parentViewModel by lazy {
        requireParentFragment().viewModel<TradeContainerViewModel>().value
    }

    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(MyMessageDelegate {
                navigate(HistoryChatFragmentDirections.toChatImageDialog(it))
            })
            registerDelegate(PartnerMessageDelegate {
                navigate(HistoryChatFragmentDirections.toChatImageDialog(it))
            })
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHistoryChatBinding =
        FragmentHistoryChatBinding.inflate(inflater, container, false)

    override fun FragmentHistoryChatBinding.initViews() {
        setToolbarTitle(args.partnerPublicId)
        binding.chatList.adapter = adapter
    }

    override fun FragmentHistoryChatBinding.initObservers() {
        viewModel.getChatHistory(args.orderId).observe(viewLifecycleOwner) {
            if (it.isRight) {
                val chatHistory = (it as Either.Right<List<ListItem>>).b
                adapter.update(chatHistory)
            } else {
                parentViewModel.showError((it as Either.Left<Failure>).a)
            }
        }
    }
}