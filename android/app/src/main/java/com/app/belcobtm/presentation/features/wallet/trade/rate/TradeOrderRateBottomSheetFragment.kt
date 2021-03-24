package com.app.belcobtm.presentation.features.wallet.trade.rate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentTradeRateBinding
import com.app.belcobtm.databinding.IncludeErrorScreenBinding
import com.app.belcobtm.presentation.core.ui.fragment.BaseBottomSheetFragment
import org.koin.android.viewmodel.ext.android.viewModel

class TradeOrderRateBottomSheetFragment : BaseBottomSheetFragment() {

    override val errorBinding: IncludeErrorScreenBinding
        get() = binding.errorView
    override val progressView: View
        get() = binding.progressView
    override val contentView: View
        get() = binding.contentGroup

    private lateinit var binding: FragmentTradeRateBinding
    private val args by navArgs<TradeOrderRateBottomSheetFragmentArgs>()
    private val viewModel by viewModel<TradeOrderRateViewModel>()
    override val retryListener: View.OnClickListener = View.OnClickListener {
        viewModel.rateOrder(args.orderId)
    }

    override fun getTheme(): Int = R.style.DialogStyle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTradeRateBinding.inflate(inflater, container, false)
        viewModel.rateLabel.observe(viewLifecycleOwner, binding.ratingValue::setText)
        viewModel.rateLoadingData.listen(success = {
            dismiss()
        })
        binding.header.text = resources.getString(R.string.trade_rate_header_formatted, args.parterPublicId)
        binding.ratingBar.rating = TradeOrderRateViewModel.GOOD_RATING.toFloat()
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            viewModel.onRateChanged(rating.toInt())
        }
        binding.rateButton.setOnClickListener {
            viewModel.rateOrder(args.orderId)
        }
        return binding.root
    }
}