package com.app.belcobtm.presentation.features.wallet.trade.order.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.OrderStatus
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.FragmentTradeOrderDetailsBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.extensions.*
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
        viewModel.traderStatusIcon.observe(viewLifecycleOwner) {
            binding.makerPublicId.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_account_circle, 0, it, 0
            )
        }
        viewModel.openRateScreen.observe(viewLifecycleOwner) { showRateDialog ->
            if (showRateDialog) {
                navigate(
                    TradeOrderDetailsFragmentDirections.toRateOrderFragment(
                        viewModel.partnerPublicId.value.orEmpty(), args.orderId
                    )
                )
            }
        }
        viewModel.makerTotalTrades.observe(viewLifecycleOwner) {
            makerTradeCountLabel.text = it.toHtmlSpan()
        }
        viewModel.orderStatus.observe(viewLifecycleOwner) {
            statusValue.setText(it.statusLabelId)
            statusValue.setDrawableEnd(it.statusDrawableId)
        }
        viewModel.partnerScore.observe(viewLifecycleOwner) {
            partnerScoreValue.text = it.toString()
            partnerScoreValue.toggle(it != null)
        }
        viewModel.myScore.observe(viewLifecycleOwner) {
            myScoreValue.text = it.toString()
            myScoreValue.toggle(it != null)
        }
        viewModel.distance.observe(viewLifecycleOwner) {
            binding.distanceLabel.text = it
            binding.distanceLabel.toggle(isVisible = true)
        }
        viewModel.makerPublicId.observe(viewLifecycleOwner, makerPublicId::setText)
        viewModel.makerTradeRate.observe(viewLifecycleOwner) {
            makerRateValue.text = it.toString()
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
        viewModel.coin.observe(viewLifecycleOwner) {
            coinIcon.setImageResource(it.resIcon())
            coinLabel.text = it.name
        }
        binding.primaryActionButton.setOnClickListener {
            viewModel.updateOrderPrimaryAction(args.orderId)
        }
        binding.secondaryActionButton.setOnClickListener {
            viewModel.updateOrderSecondaryAction(args.orderId)
        }

        binding.distanceLabel.setOnClickListener {
            val gmmIntentUri = Uri.parse(viewModel.getQueryForMap())
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage(requireContext().getString(R.string.google_maps_package))
            startActivity(mapIntent)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.chat_menu_item -> {
            val statusId = viewModel.orderStatus.value?.statusId
            if (statusId == OrderStatus.NEW || statusId == OrderStatus.DOING || statusId == OrderStatus.PAID) {
                navigate(
                    TradeOrderDetailsFragmentDirections.toChatOrderFragment(
                        viewModel.partnerPublicId.value.orEmpty(), args.orderId,
                        viewModel.myId.value ?: 0, viewModel.partnerId.value ?: 0
                    )
                )
            } else {
                navigate(
                    TradeOrderDetailsFragmentDirections.toHistoryChatOrderFragment(
                        viewModel.partnerPublicId.value.orEmpty(), args.orderId
                    )
                )
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}