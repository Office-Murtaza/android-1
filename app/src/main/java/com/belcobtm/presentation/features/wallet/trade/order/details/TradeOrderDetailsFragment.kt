package com.belcobtm.presentation.features.wallet.trade.order.details

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.data.model.trade.OrderStatus
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.databinding.FragmentTradeOrderDetailsBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.wallet.trade.list.delegate.TradePaymentOptionDelegate
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeDrawable.TOP_END
import com.google.android.material.badge.BadgeUtils
import org.koin.androidx.viewmodel.ext.android.viewModel

class TradeOrderDetailsFragment : BaseFragment<FragmentTradeOrderDetailsBinding>() {

    override val isHomeButtonEnabled: Boolean
        get() = true

    override val retryListener: View.OnClickListener = View.OnClickListener {
        val initialLoadingState: LoadingData<Unit>? = viewModel.initialLoadingData.value
        val primaryActionLoadingState: LoadingData<Unit>? =
            viewModel.primaryActionUpdateLoadingData.value
        val secondaryActionLoadingState: LoadingData<Unit>? =
            viewModel.secondaryActionUpdateLoadingData.value
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

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTradeOrderDetailsBinding =
        FragmentTradeOrderDetailsBinding.inflate(inflater, container, false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        viewModel.fetchInitialData(args.orderId)
        return root
    }

    override fun FragmentTradeOrderDetailsBinding.initViews() {
        setToolbarTitle(R.string.trade_order_details_screen_title)
        paymentOptions.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.connectToChat()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnectFromChat()
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun FragmentTradeOrderDetailsBinding.initObservers() {
        viewModel.initialLoadingData.listen()
        viewModel.primaryActionUpdateLoadingData.listen()
        viewModel.secondaryActionUpdateLoadingData.listen()
        viewModel.price.observe(viewLifecycleOwner, price::setText)
        viewModel.paymentOptions.observe(viewLifecycleOwner, adapter::update)
        viewModel.traderStatusIcon.observe(viewLifecycleOwner) {
            binding.partnerPublicId.setCompoundDrawablesWithIntrinsicBounds(
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
        viewModel.partnerTotalTrades.observe(viewLifecycleOwner) {
            partnerTradeCountLabel.text = it.toHtmlSpan()
        }
        viewModel.orderId.observe(viewLifecycleOwner) {
            orderIdValue.text = it
        }
        viewModel.orderStatus.observe(viewLifecycleOwner) {
            statusValue.setText(it.statusLabelId)
            statusValue.setDrawableEnd(it.statusDrawableId)
            ratingGroup.toggle(it.statusId == OrderStatus.RELEASED || it.statusId == OrderStatus.SOLVED)
        }
        viewModel.partnerScore.observe(viewLifecycleOwner) {
            if (it == null) {
                partnerScoreValue.setText(R.string.trade_order_details_not_rated_label)
                partnerScoreValue.setDrawableStart(0)
            } else {
                partnerScoreValue.text = it.toString()
                partnerScoreValue.setDrawableStart(R.drawable.ic_grade)
            }
        }
        viewModel.myScore.observe(viewLifecycleOwner) {
            if (it == null) {
                myScoreValue.setText(R.string.trade_order_details_not_rated_label)
                myScoreValue.setDrawableStart(0)
            } else {
                myScoreValue.text = it.toString()
                myScoreValue.setDrawableStart(R.drawable.ic_grade)
            }
        }
        viewModel.distance.observe(viewLifecycleOwner) {
            binding.distanceLabel.text = it
            binding.distanceLabel.toggle(isVisible = it.orEmpty().isNotEmpty())
        }
        viewModel.partnerPublicId.observe(viewLifecycleOwner, partnerPublicId::setText)
        viewModel.partnerTradeRate.observe(viewLifecycleOwner) {
            partnerRateValue.text = it.toString()
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
                    setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.trade_type_buy_trade_text_color
                        )
                    )
                } else {
                    setBackgroundResource(R.drawable.trade_type_sell_background)
                    setDrawableStart(R.drawable.ic_trade_type_sell)
                    setText(R.string.trade_type_sell_label)
                    setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.trade_type_sell_trade_text_color
                        )
                    )
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
        viewModel.observeMissedMessageCount(args.orderId).observe(viewLifecycleOwner) {
            val badge = BadgeDrawable.create(requireContext())
            badge.badgeGravity = TOP_END
            badge.backgroundColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            badge.badgeTextColor = ContextCompat.getColor(requireContext(), R.color.gph_white)
            BadgeUtils.detachBadgeDrawable(badge, baseBinding.toolbarView, R.id.chat_menu_item)
            if (it > 0) {
                badge.number = it
                BadgeUtils.attachBadgeDrawable(badge, baseBinding.toolbarView, R.id.chat_menu_item)
            }
        }
        binding.distanceLabel.setOnClickListener {
            val gmmIntentUri = Uri.parse(viewModel.getQueryForMap())
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage(requireContext().getString(R.string.google_maps_package))
            startActivity(mapIntent)
        }
        binding.icOrderIdCopy.setOnClickListener {
            copyToClipboard(viewModel.orderId.value.orEmpty())
        }
    }

    private fun copyToClipboard(copiedText: String) {
        val clipboard =
            requireContext().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(copiedText, copiedText)
        clipboard.setPrimaryClip(clip)
        AlertHelper.showToastShort(requireContext(), R.string.copied)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.chat_menu_item -> {
            if (viewModel.isActiveOrder()) {
                navigate(
                    TradeOrderDetailsFragmentDirections.toChatOrderFragment(
                        viewModel.partnerPublicId.value.orEmpty(), args.orderId,
                        viewModel.myId.value.orEmpty(), viewModel.partnerId.value.orEmpty()
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