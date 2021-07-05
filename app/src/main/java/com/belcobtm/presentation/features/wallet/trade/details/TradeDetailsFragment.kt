package com.belcobtm.presentation.features.wallet.trade.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.databinding.FragmentTradeDetailsBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.extensions.resIcon
import com.belcobtm.presentation.core.extensions.toHtmlSpan
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.wallet.trade.list.delegate.TradePaymentOptionDelegate
import org.koin.android.viewmodel.ext.android.viewModel

class TradeDetailsFragment : BaseFragment<FragmentTradeDetailsBinding>() {

    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true

    private val args by navArgs<TradeDetailsFragmentArgs>()
    private val viewModel by viewModel<TradeDetailsViewModel>()
    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(TradePaymentOptionDelegate())
        }
    }

    override val retryListener: View.OnClickListener = View.OnClickListener {
        viewModel.fetchTradeDetails(args.tradeId)
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeDetailsBinding =
        FragmentTradeDetailsBinding.inflate(inflater, container, false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        viewModel.fetchTradeDetails(args.tradeId)
        return root
    }

    override fun FragmentTradeDetailsBinding.initListeners() {
        buySellButton.setOnClickListener {
            navigate(TradeDetailsFragmentDirections.toTradeDetailsBuySell(args.tradeId))
        }
    }

    override fun FragmentTradeDetailsBinding.initViews() {
        setToolbarTitle(R.string.my_trade_details_screen_title)
        paymentOptions.adapter = adapter
    }

    override fun FragmentTradeDetailsBinding.initObservers() {
        viewModel.initialLoadingData.listen()
        viewModel.price.observe(viewLifecycleOwner, price::setText)
        viewModel.paymentOptions.observe(viewLifecycleOwner, adapter::update)
        viewModel.publicId.observe(viewLifecycleOwner, makerPublicId::setText)
        viewModel.traderRate.observe(viewLifecycleOwner) {
            makerRateLabel.text = it.toString()
        }
        viewModel.traderStatus.observe(viewLifecycleOwner) {
            binding.makerPublicId.setCompoundDrawablesWithIntrinsicBounds(0, 0, it, 0)
        }
        viewModel.totalTrades.observe(viewLifecycleOwner) {
            makerTradeCountLabel.text = it.toHtmlSpan()
        }
        viewModel.distance.observe(viewLifecycleOwner) {
            binding.distanceLabel.text = it
            binding.distanceLabel.toggle(isVisible = true)
        }
        viewModel.terms.observe(viewLifecycleOwner, terms::setText)
        viewModel.amountRange.observe(viewLifecycleOwner, amountRange::setText)
        viewModel.tradeType.observe(viewLifecycleOwner) {
            if (it == TradeType.BUY) {
                buySellButton.setText(R.string.trade_details_sell_button_title)
            } else {
                buySellButton.setText(R.string.trade_details_buy_button_title)
            }
        }
        viewModel.selectedCoin.observe(viewLifecycleOwner) {
            coinIcon.setImageResource(it.resIcon())
            coinLabel.text = it.name
        }
        binding.distanceLabel.setOnClickListener {
            val gmmIntentUri = Uri.parse(viewModel.getQueryForMap())
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage(requireContext().getString(R.string.google_maps_package))
            startActivity(mapIntent)
        }
    }
}