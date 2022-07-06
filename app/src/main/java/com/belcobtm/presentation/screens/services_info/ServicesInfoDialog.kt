package com.belcobtm.presentation.screens.services_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.databinding.FragmentServicesInfoBinding
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

// TODO:: rework this piece of shit :)
class ServicesInfoDialog : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentServicesInfoBinding

    private val viewModel: ServicesInfoViewModel by viewModel()

    private val args by navArgs<ServicesInfoDialogArgs>()

    private val currencyFormatter: Formatter<Double> by inject(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServicesInfoBinding.inflate(inflater, container, false)
        getServiceInfo(binding)
        return binding.root
    }

    private fun getServiceInfo(binding: FragmentServicesInfoBinding) {
        viewModel.getService(args.type)
        observeViewModel(binding)
    }

    private fun observeViewModel(binding: FragmentServicesInfoBinding) {
        viewModel.serviceLiveData.observe(viewLifecycleOwner) {
            binding.txLimitValueView.text = currencyFormatter.format(it.txLimit)
            binding.dailyLimitValueView.text = currencyFormatter.format(it.dailyLimit)
            binding.remainLimitValueView.text = currencyFormatter.format(it.remainLimit)
        }
    }

}
