package com.belcobtm.presentation.features.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.databinding.FragmentServicesInfoBinding
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.presentation.tools.formatter.DoubleCurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class ServicesInfoBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentServicesInfoBinding

    private val serviceInfoProvider: ServiceInfoProvider by inject()
    private val args by navArgs<ServicesInfoBottomSheetDialogFragmentArgs>()
    private val currencyFormatter: Formatter<Double> by inject(
        named(DoubleCurrencyPriceFormatter.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServicesInfoBinding.inflate(inflater, container, false)
        val service = serviceInfoProvider.getService(args.type)
        binding.txLimitValueView.text = currencyFormatter.format(service?.txLimit ?: 0.0)
        binding.dailyLimitValueView.text = currencyFormatter.format(service?.dailyLimit ?: 0.0)
        binding.remainLimitValueView.text = currencyFormatter.format(service?.remainLimit ?: 0.0)
        return binding.root
    }
}