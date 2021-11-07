package com.belcobtm.presentation.features.wallet.trade.order.rate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeRateBinding
import com.belcobtm.databinding.IncludeErrorScreenBinding
import com.belcobtm.presentation.core.ui.fragment.BaseBottomSheetFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTradeRateBinding.inflate(inflater, container, false)
        viewModel.rateLabel.observe(viewLifecycleOwner, binding.ratingValue::setText)
        viewModel.rateLoadingData.listen(success = {
            findNavController().popBackStack(R.id.trade_container_fragment, false)
        })
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