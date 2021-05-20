package com.app.belcobtm.presentation.features.wallet.trade.list.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.filter.SortOption
import com.app.belcobtm.databinding.FragmentTradeFilterBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.extensions.setTextSilently
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.app.belcobtm.presentation.features.wallet.trade.create.delegate.TradePaymentOptionDelegate
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.delegate.ItemTradeFilterCoinCodeDelegate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.viewmodel.ext.android.viewModel

class TradeFilterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val coinsAdapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply { registerDelegate(ItemTradeFilterCoinCodeDelegate(viewModel::selectCoin)) }
    }

    private val paymentsAdapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply { registerDelegate(TradePaymentOptionDelegate()) }
    }

    private val viewModel by viewModel<TradeFilterViewModel>()
    private lateinit var binding: FragmentTradeFilterBinding
    private val minDistanceValue by lazy { resources.getInteger(R.integer.trade_filter_min_distance) }
    private val maxDistanceValue by lazy { resources.getInteger(R.integer.trade_filter_max_distance) }

    private val minDistanceTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val currentMax = binding.distanceMaxLimitEditText.text?.toString()
            ?.let(viewModel::parseDistance)?.toInt() ?: maxDistanceValue
        val parsedAmount = viewModel.parseDistance(editable.toString()).coerceAtMost(currentMax)
        viewModel.updateMinDistance(parsedAmount)
    }

    private val maxDistanceTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = viewModel.parseDistance(editable.toString()).coerceAtMost(maxDistanceValue)
        viewModel.updateMaxDistance(parsedAmount)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTradeFilterBinding.inflate(inflater, container, false)
        binding.coins.adapter = coinsAdapter
        binding.paymentOptions.adapter = paymentsAdapter
        viewModel.fetchInitialData()
        binding.distanceMaxLimitEditText.addTextChangedListener(maxDistanceTextWatcher)
        binding.distanceMinLimitEditText.addTextChangedListener(minDistanceTextWatcher)
        viewModel.updateMinDistance(minDistanceValue)
        viewModel.updateMaxDistance(maxDistanceValue)
        viewModel.coins.observe(viewLifecycleOwner, coinsAdapter::update)
        viewModel.paymentOptions.observe(viewLifecycleOwner, paymentsAdapter::update)
        viewModel.distanceEnabled.observe(viewLifecycleOwner) {
            with(binding) {
                distanceLabel.toggle(it)
                distanceMinLimitInputLayout.toggle(it)
                distanceMaxLimitInputLayout.toggle(it)
                distanceRangeSliderDivider.toggle(it)
                sortByDistance.toggle(it)
            }
        }
        viewModel.sortOption.observe(viewLifecycleOwner) {
            when (it) {
                SortOption.PRICE -> binding.sortByPrice.isChecked = true
                SortOption.DISTANCE -> binding.sortByDistance.isChecked = true
            }
        }
        viewModel.distanceMinLimit.observe(viewLifecycleOwner) { distance ->
            binding.distanceMinLimitEditText.setTextSilently(
                minDistanceTextWatcher, distance.toString(), distance.toString().length
            )
        }
        viewModel.distanceRangeError.observe(viewLifecycleOwner) {
            it?.let(binding.distanceRangeError::setText)
            binding.distanceRangeError.toggle(it != null)
        }
        viewModel.closeFilter.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        binding.sortChipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.sort_by_price -> viewModel.selectSort(SortOption.PRICE)
                R.id.sort_by_distance -> viewModel.selectSort(SortOption.DISTANCE)
                else -> {
                    binding.sortByPrice.isChecked = true
                }
            }
        }
        binding.sortByPrice.setOnCheckedChangeListener { _, isChecked ->
            binding.sortByPrice.chipStrokeWidth = if (isChecked) {
                resources.getDimensionPixelSize(R.dimen.divider_size).toFloat()
            } else {
                0.0f
            }
        }
        binding.sortByDistance.setOnCheckedChangeListener { _, isChecked ->
            binding.sortByDistance.chipStrokeWidth = if (isChecked) {
                resources.getDimensionPixelSize(R.dimen.divider_size).toFloat()
            } else {
                0.0f
            }
        }
        binding.resetFilters.setOnClickListener {
            viewModel.resetFilter()
        }
        binding.applyFilterButton.setOnClickListener {
            viewModel.applyFilter(minDistanceValue, maxDistanceValue)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        forceExpand()
    }

    private fun forceExpand() {
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                (dialog as BottomSheetDialog?)?.let { dialog ->
                    val bottomSheet = dialog.findViewById<FrameLayout>(
                        com.google.android.material.R.id.design_bottom_sheet
                    )
                    if (bottomSheet != null) {
                        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
                requireView().viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        requireView().viewTreeObserver.addOnGlobalLayoutListener(listener)
    }
}