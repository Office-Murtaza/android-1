package com.app.belcobtm.presentation.features.wallet.trade.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.FragmentTradeDetailsBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.list.delegate.TradePaymentOptionDelegate
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
            makerTradeCountLabel.text = resources.getString(R.string.trade_details_screen_total_trades_formatted, it)
        }
        viewModel.distance.observe(viewLifecycleOwner) {
            binding.distanceLabel.text = it
            binding.distanceLabel.toggle(isVisible = true)
        }
        viewModel.terms.observe(viewLifecycleOwner, terms::setText)
        viewModel.amountRange.observe(viewLifecycleOwner, amountRange::setText)
        viewModel.tradeType.observe(viewLifecycleOwner) {
            with(tradeTypeChip) {
                if (it == TradeType.BUY) {
                    setBackgroundResource(R.drawable.trade_type_buy_background)
                    setChipIconResource(R.drawable.ic_trade_type_buy)
                    setText(R.string.trade_type_buy_label)
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.trade_type_buy_trade_text_color
                        )
                    )
                } else {
                    setBackgroundResource(R.drawable.trade_type_sell_background)
                    setChipIconResource(R.drawable.ic_trade_type_sell)
                    setText(R.string.trade_type_sell_label)
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.trade_type_sell_trade_text_color
                        )
                    )
                }
            }
        }
        viewModel.selectedCoin.observe(viewLifecycleOwner) {
            coinIcon.setImageResource(it.resIcon())
            coinLabel.text = it.name
        }
    }
}