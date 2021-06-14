package com.app.belcobtm.presentation.features.wallet.trade.order.create

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeCreateOrderBinding
import com.app.belcobtm.databinding.IncludeErrorScreenBinding
import com.app.belcobtm.presentation.core.extensions.getDouble
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringPercents
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        viewModel.updateAmount(editable.getDouble())
    }

    override fun getTheme(): Int = R.style.DialogStyle

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTradeCreateOrderBinding.inflate(inflater, container, false)
        viewModel.fetchTradeDetails(args.tradeId)
        binding.amountEditText.setText("0")
        binding.amountEditText.addTextChangedListener(amountTextWatcher)
        viewModel.initialLoadingData.listen()
        viewModel.createTradeLoadingData.listen(success = {
            findNavController().navigate(TradeCreateOrderBottomSheetFragmentDirections.toOrderDetails(it))
        })
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
        viewModel.reservedBalance.observe(viewLifecycleOwner) {
            binding.reservedAmountLabel.text = getString(
                R.string.create_order_reserved_amount, it.reservedBalanceCrypto.toStringCoin(), it.coinName
            )
        }
        viewModel.amountWithoutFee.observe(viewLifecycleOwner) {
            binding.totalCryptoValue.text = getString(
                R.string.create_order_total_amount, it.totalValueCrypto.toStringCoin(), it.coinName
            )
        }
        viewModel.receiveAmountLabel.observe(viewLifecycleOwner) {
            binding.totalCryptoLabel.setText(it)
        }
        binding.submitButton.setOnClickListener {
            viewModel.createOrder()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.amountEditText.post {
            binding.amountEditText.requestFocus()
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }
}