package com.belcobtm.presentation.features.wallet.trade.order.create

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
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeCreateOrderBinding
import com.belcobtm.databinding.IncludeErrorScreenBinding
import com.belcobtm.presentation.core.extensions.getDouble
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
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
    private val imm by lazy {
        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    }

    override fun getTheme(): Int = R.style.DialogStyle

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).state =
                BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTradeCreateOrderBinding.inflate(inflater, container, false)
        viewModel.fetchTradeDetails(args.tradeId)
        binding.amountEditText.addTextChangedListener(amountTextWatcher)
        viewModel.initialLoadingData.listen()
        viewModel.createTradeLoadingData.listen(success = {
            findNavController().navigate(
                TradeCreateOrderBottomSheetFragmentDirections.toOrderDetails(
                    it
                )
            )
        })
        viewModel.platformFee.observe(viewLifecycleOwner) {
            binding.feeAmountValue.text = it.platformFeeCrypto.toStringCoin()
                .plus(" ").plus(it.coinCode)
        }
        viewModel.cryptoAmount.observe(viewLifecycleOwner) {
            binding.cryptoAmountValue.text = it.cryptoAmount.toStringCoin()
                .plus(" ").plus(it.coinCode)
        }
        viewModel.fiatAmountError.observe(viewLifecycleOwner) {
            binding.amountError.text = it
            binding.amountError.toggle(it != null)
        }
        viewModel.reservedBalance.observe(viewLifecycleOwner) {
            binding.reservedAmountValue.text = it.reservedBalanceCrypto.toStringCoin()
                .plus(" ").plus(it.coinName)
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
            imm?.showSoftInput(binding.amountEditText, 0)
        }
    }

    override fun onDestroyView() {
        imm?.hideSoftInputFromWindow(binding.amountEditText.windowToken, 0)
        binding.amountEditText.clearFocus()
        super.onDestroyView()
    }
}