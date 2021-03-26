package com.app.belcobtm.presentation.features.wallet.trade.order.historychat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.databinding.FragmentHistoryChatBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.delegate.MyMessageDelegate
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.delegate.PartnerMessageDelegate
import org.koin.android.viewmodel.ext.android.viewModel

class HistoryChatFragment : BaseFragment<FragmentHistoryChatBinding>() {

    override var isHomeButtonEnabled: Boolean = true
    private val viewModel by viewModel<HistoryChatViewModel>()
    private val args by navArgs<HistoryChatFragmentArgs>()

    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(MyMessageDelegate())
            registerDelegate(PartnerMessageDelegate())
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHistoryChatBinding =
        FragmentHistoryChatBinding.inflate(inflater, container, false)

    override fun FragmentHistoryChatBinding.initViews() {
        setToolbarTitle(args.partnerPublicId)
        binding.chatList.adapter = adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        viewModel.loadChatHistory(args.orderId)
        return root
    }

    override fun FragmentHistoryChatBinding.initObservers() {
        viewModel.chatContent.observe(viewLifecycleOwner, adapter::update)
    }
}