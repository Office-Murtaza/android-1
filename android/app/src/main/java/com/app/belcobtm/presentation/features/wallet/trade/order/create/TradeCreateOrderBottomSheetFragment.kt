package com.app.belcobtm.presentation.features.wallet.trade.order.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeCreateOrderBinding
import com.app.belcobtm.databinding.IncludeErrorScreenBinding
import com.app.belcobtm.presentation.core.extensions.setTextSilently
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringPercents
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import org.koin.android.viewmodel.ext.android.viewModel

class TradeCreateOrderBottomSheetFragment : BaseBottomSheetFragment() {

    private lateinit var binding: FragmentTradeCreateOrderBinding

    private val args by navArgs<TradeCreateOrderBottomSheetFragmentArgs>()
    private val viewModel by viewModel<TradeCreateOrderViewModel>()

    override val errorBinding: IncludeErrorScreenBinding
        get() = binding.errorView

    override val progressView: View
        get() = binding.progressView

    override val contentView: View
        get() = binding.contentGroup

    override val retryListener: View.OnClickListener = View.OnClickListener {
        if (viewModel.initialLoadingData.value is LoadingData.Error) {
            viewModel.fetchTradeDetails(args.tradeId)
        } else {
            viewModel.createOrder()
        }
    }
    private val amountTextWatcher = SafeDecimalEditTextWatcher { editable ->
        viewModel.updateAmount(viewModel.parseAmount(editable.toString()) / 100)
    }

    override fun getTheme(): Int = R.style.DialogStyle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTradeCreateOrderBinding.inflate(inflater, container, false)
        viewModel.fetchTradeDetails(args.tradeId)
        binding.amountEditText.addTextChangedListener(amountTextWatcher)
        viewModel.initialLoadingData.listen()
        viewModel.createTradeLoadingData.listen(success = {
            findNavController().navigate(TradeCreateOrderBottomSheetFragmentDirections.toOrderDetails(it))
        })
        viewModel.fiatAmount.observe(viewLifecycleOwner) {
            binding.amountEditText.setTextSilently(amountTextWatcher, viewModel.formatAmount(it))
        }
        viewModel.platformFee.observe(viewLifecycleOwner) {
            binding.platformFeeLabel.text = requireContext().resources.getString(
                R.string.trade_buy_sell_dialog_platform_fee_formatted,
                it.platformFeePercent.toStringPercents(), it.platformFeeCrypto.toStringCoin(), it.coinCode
            )
        }
        viewModel.cryptoAmount.observe(viewLifecycleOwner) {
            binding.cryptoAmountValue.text = requireContext().resources.getString(
                R.string.trade_buy_sell_dialog_crypto_amount_formatted,
                it.cryptoAmount.toStringCoin(), it.coinCode
            )
        }
        viewModel.fiatAmountError.observe(viewLifecycleOwner) {
            binding.amountError.text = it
            binding.amountError.toggle(it != null)
        }
        binding.submitButton.setOnClickListener {
            viewModel.createOrder()
        }
        return binding.root
    }
}