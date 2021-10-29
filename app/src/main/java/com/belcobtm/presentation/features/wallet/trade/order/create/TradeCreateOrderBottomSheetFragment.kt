package com.belcobtm.presentation.features.wallet.trade.order.create

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeCreateOrderBinding
import com.belcobtm.databinding.IncludeErrorScreenBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.extensions.getDouble
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseBottomSheetFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

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
        val blackColorSpan = ForegroundColorSpan(
            ContextCompat.getColor(
                requireContext(),
                R.color.black_text_color
            )
        )
        binding = FragmentTradeCreateOrderBinding.inflate(inflater, container, false)
        viewModel.fetchTradeDetails(args.tradeId)
        binding.amountEditText.setText("0")
        binding.amountEditText.addTextChangedListener(amountTextWatcher)
        viewModel.initialLoadingData.listen()
        viewModel.createTradeLoadingData.listen(success = {
            findNavController().navigate(
                TradeCreateOrderBottomSheetFragmentDirections.toOrderDetails(
                    it
                )
            )
        }, error = {
            if (it !is Failure.ValidationError) {
                baseErrorHandler(it)
            } else {
                showContent()
            }
        })
        viewModel.platformFee.observe(viewLifecycleOwner) {
            val cryptoFormatted = getString(
                R.string.trade_crypto_amount_value,
                it.platformFeeCrypto.toStringCoin(),
                it.coinCode
            )
            val formattedSpan = SpannableString(
                getString(
                    R.string.create_order_dialog_platform_fee_format, cryptoFormatted
                )
            )
            val start = formattedSpan.indexOf(cryptoFormatted)
            formattedSpan.setSpan(
                blackColorSpan,
                start,
                start + cryptoFormatted.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            binding.platformFee.text = formattedSpan
        }
        viewModel.cryptoAmount.observe(viewLifecycleOwner) {
            val cryptoFormatted = getString(
                R.string.trade_crypto_amount_value,
                it.cryptoAmount.toStringCoin(),
                it.coinCode
            )
            val formattedSpan = SpannableString(
                getString(
                    R.string.create_order_dialog_crypto_amount_format, cryptoFormatted
                )
            )
            val start = formattedSpan.indexOf(cryptoFormatted)
            formattedSpan.setSpan(
                blackColorSpan,
                start,
                start + cryptoFormatted.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            binding.cryptoAmount.text = formattedSpan
        }
        viewModel.fiatAmountError.observe(viewLifecycleOwner) {
            binding.amountError.text = it
            binding.amountError.toggle(it != null)
        }
        viewModel.reservedBalance.observe(viewLifecycleOwner) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val uri = getString(R.string.reserved_deeplink_format, it.coinName).toUri()
                    findNavController().navigate(uri)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            val cryptoFormatted = getString(
                R.string.coin_balance_format,
                it.reservedBalanceCrypto.toStringCoin(),
                it.coinName,
                it.reservedBalanceUsd
            )
            val formattedSpan = SpannableString(
                getString(
                    R.string.create_order_dialog_reserved_format, cryptoFormatted
                )
            )
            val start = formattedSpan.indexOf(cryptoFormatted)
            formattedSpan.setSpan(
                clickableSpan,
                start,
                start + cryptoFormatted.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            binding.reservedAmount.text = formattedSpan
        }
        binding.reservedAmount.movementMethod = LinkMovementMethod.getInstance()
        viewModel.amountWithoutFee.observe(viewLifecycleOwner) {
            val cryptoFormatted = getString(
                R.string.create_order_total_amount, it.totalValueCrypto.toStringCoin(), it.coinName
            )
            val formattedSpan = SpannableString(getString(it.labelId, cryptoFormatted))
            val start = formattedSpan.indexOf(cryptoFormatted)
            formattedSpan.setSpan(
                blackColorSpan,
                start,
                start + cryptoFormatted.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            binding.totalCrypto.text = formattedSpan
        }
        binding.submitButton.setOnClickListener {
            viewModel.createOrder()
        }
        return binding.root
    }

    override fun onDestroyView() {
        imm?.hideSoftInputFromWindow(binding.amountEditText.windowToken, 0)
        binding.amountEditText.clearFocus()
        super.onDestroyView()
    }
}