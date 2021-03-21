package com.app.belcobtm.presentation.features.wallet.trade.order.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.FragmentTradeOrderDetailsBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.setDrawableEnd
import com.app.belcobtm.presentation.core.extensions.setDrawableStart
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.list.delegate.TradePaymentOptionDelegate
import org.koin.android.viewmodel.ext.android.viewModel

class TradeOrderDetailsFragment : BaseFragment<FragmentTradeOrderDetailsBinding>() {

    override val isHomeButtonEnabled: Boolean
        get() = true

    override val retryListener: View.OnClickListener? = View.OnClickListener {
        val initialLoadingState: LoadingData<Unit>? = viewModel.initialLoadingData.value
        val primaryActionLoadingState: LoadingData<Unit>? = viewModel.primaryActionUpdateLoadingData.value
        val secondaryActionLoadingState: LoadingData<Unit>? = viewModel.secondaryActionUpdateLoadingData.value
        when {
            initialLoadingState != null && initialLoadingState is LoadingData.Error ->
                viewModel.fetchInitialData(args.orderId)
            primaryActionLoadingState != null && primaryActionLoadingState is LoadingData.Error ->
                viewModel.updateOrderPrimaryAction(args.orderId)
            secondaryActionLoadingState != null && secondaryActionLoadingState is LoadingData.Error ->
                viewModel.updateOrderSecondaryAction(args.orderId)
        }
    }
    private val args by navArgs<TradeOrderDetailsFragmentArgs>()
    private val viewModel by viewModel<TradeOrderDetailsViewModel>()
    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(TradePaymentOptionDelegate())
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTradeOrderDetailsBinding =
        FragmentTradeOrderDetailsBinding.inflate(inflater, container, false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        viewModel.fetchInitialData(args.orderId)
        return root
    }

    override fun FragmentTradeOrderDetailsBinding.initViews() {
        setToolbarTitle(R.string.trade_order_details_screen_title)
        paymentOptions.adapter = adapter
    }

    override fun FragmentTradeOrderDetailsBinding.initObservers() {
        viewModel.initialLoadingData.listen()
        viewModel.primaryActionUpdateLoadingData.listen()
        viewModel.secondaryActionUpdateLoadingData.listen()
        viewModel.price.observe(viewLifecycleOwner, price::setText)
        viewModel.paymentOptions.observe(viewLifecycleOwner, adapter::update)
        viewModel.traderStatus.observe(viewLifecycleOwner) {
            binding.makerPublicId.setCompoundDrawablesWithIntrinsicBounds(0, 0, it, 0)
        }
        viewModel.partnerPublicId.observe(viewLifecycleOwner, makerPublicId::setText)
        viewModel.partnerTotalTrades.observe(viewLifecycleOwner) {
            makerTradeCountLabel.text = resources.getString(R.string.trade_details_screen_total_trades_formatted, it)
        }
        viewModel.orderStatus.observe(viewLifecycleOwner) {
            statusValue.setText(it.statusLabelId)
            statusValue.setDrawableEnd(it.statusDrawableId)
        }
        viewModel.partnerScore.observe(viewLifecycleOwner) {
            makerRateValue.text = it.toString()
            partnerScoreValue.text = it.toString()
        }
        viewModel.myScore.observe(viewLifecycleOwner) {
            myScoreValue.text = it.toString()
        }
        viewModel.distance.observe(viewLifecycleOwner) {
            binding.distanceLabel.text = it
            binding.distanceLabel.toggle(isVisible = true)
        }
        viewModel.fiatAmount.observe(viewLifecycleOwner, fiatAmountValue::setText)
        viewModel.cryptoAmount.observe(viewLifecycleOwner, cryptoAmountValue::setText)
        viewModel.terms.observe(viewLifecycleOwner, terms::setText)
        viewModel.buttonsState.observe(viewLifecycleOwner) {
            if (it.showPrimaryButton) {
                binding.primaryActionButton.setText(it.primaryButtonTitleRes)
            }
            if (it.showSecondaryButton) {
                binding.secondaryActionButton.setText(it.secondaryButtonTitleRes)
            }
            binding.primaryActionButton.toggle(it.showPrimaryButton)
            binding.secondaryActionButton.toggle(it.showSecondaryButton)
        }
        viewModel.tradeType.observe(viewLifecycleOwner) {
            with(tradeType) {
                if (it == TradeType.BUY) {
                    setBackgroundResource(R.drawable.trade_type_buy_background)
                    setDrawableStart(R.drawable.ic_trade_type_buy)
                    setText(R.string.trade_type_buy_label)
                    setTextColor(ContextCompat.getColor(binding.root.context, R.color.trade_type_buy_trade_text_color))
                } else {
                    setBackgroundResource(R.drawable.trade_type_sell_background)
                    setDrawableStart(R.drawable.ic_trade_type_sell)
                    setText(R.string.trade_type_sell_label)
                    setTextColor(ContextCompat.getColor(binding.root.context, R.color.trade_type_sell_trade_text_color))
                }
            }
        }
        binding.primaryActionButton.setOnClickListener {
            viewModel.updateOrderPrimaryAction(args.orderId)
        }
        binding.secondaryActionButton.setOnClickListener {
            viewModel.updateOrderSecondaryAction(args.orderId)
        }
        viewModel.coin.observe(viewLifecycleOwner) {
            coinIcon.setImageResource(it.resIcon())
            coinLabel.text = it.name
        }
    }
}