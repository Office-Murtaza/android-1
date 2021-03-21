package com.app.belcobtm.presentation.features.wallet.trade.buysell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeCreateOrderBinding
import com.app.belcobtm.presentation.core.extensions.setTextSilently
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringPercents
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.viewmodel.ext.android.viewModel

class TradeCreateOrderBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentTradeCreateOrderBinding

    private val args by navArgs<TradeCreateOrderBottomSheetFragmentArgs>()
    private val viewModel by viewModel<TradeCreateOrderViewModel>()

    private val amountTextWatcher = SafeDecimalEditTextWatcher { editable ->
        viewModel.updateAmount(viewModel.parseAmount(editable.toString()) / 100)
    }

    override fun getTheme(): Int = R.style.CreateTradeDialogStyle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTradeCreateOrderBinding.inflate(inflater, container, false)
        viewModel.fetchTradeDetails(args.tradeId)
        binding.amountEditText.addTextChangedListener(amountTextWatcher)
        viewModel.initialLoadingData.observe(viewLifecycleOwner) {
            // TODO handle loading
        }
        viewModel.createTradeLoadingData.observe(viewLifecycleOwner) {
            // TODO handle loading
            if (it is LoadingData.Success<Int>) {
                findNavController().navigate(TradeCreateOrderBottomSheetFragmentDirections.toOrderDetails(it.data))
            }
        }
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