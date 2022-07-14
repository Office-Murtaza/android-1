package com.belcobtm.presentation.screens.wallet.trade.order.historychat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.databinding.FragmentHistoryChatBinding
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.screens.wallet.trade.order.chat.delegate.MyMessageDelegate
import com.belcobtm.presentation.screens.wallet.trade.order.chat.delegate.PartnerMessageDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryChatFragment : BaseFragment<FragmentHistoryChatBinding>() {

    override var isBackButtonEnabled: Boolean = true

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
        binding.chatRecyclerView.adapter = adapter
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
