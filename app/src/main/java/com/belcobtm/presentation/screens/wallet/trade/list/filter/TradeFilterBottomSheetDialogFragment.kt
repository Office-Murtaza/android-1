package com.belcobtm.presentation.screens.wallet.trade.list.filter

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.navigation.fragment.findNavController
import com.belcobtm.R
import com.belcobtm.domain.trade.model.filter.SortOption
import com.belcobtm.databinding.FragmentTradeFilterBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.setTextSilently
import com.belcobtm.presentation.tools.extensions.toggle
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.screens.wallet.trade.create.delegate.TradePaymentOptionDelegate
import com.belcobtm.presentation.screens.wallet.trade.list.filter.delegate.ItemTradeFilterCoinCodeDelegate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class TradeFilterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val coinsAdapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply { registerDelegate(ItemTradeFilterCoinCodeDelegate(viewModel::selectCoin)) }
    }

    private val paymentsAdapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply { registerDelegate(TradePaymentOptionDelegate(viewModel::changePaymentSelection)) }
    }

    private val viewModel by viewModel<TradeFilterViewModel>()
    private lateinit var binding: FragmentTradeFilterBinding

    private val minDistanceTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = viewModel.parseDistance(editable.toString())
        viewModel.updateMinDistance(parsedAmount)
    }

    override fun getTheme(): Int = R.style.DialogStyle

    private val maxDistanceTextWatcher = SafeDecimalEditTextWatcher { editable ->
        val parsedAmount = viewModel.parseDistance(editable.toString())
        viewModel.updateMaxDistance(parsedAmount)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTradeFilterBinding.inflate(inflater, container, false)
        binding.coins.adapter = coinsAdapter
        binding.paymentOptions.adapter = paymentsAdapter
        viewModel.fetchInitialData()
        binding.distanceMinLimitEditText.addTextChangedListener(minDistanceTextWatcher)
        binding.distanceMaxLimitEditText.addTextChangedListener(maxDistanceTextWatcher)
        viewModel.coins.observe(viewLifecycleOwner, coinsAdapter::update)
        viewModel.paymentOptions.observe(viewLifecycleOwner, paymentsAdapter::update)
        viewModel.distanceEnabled.observe(viewLifecycleOwner) {
            with(binding) {
                distanceLabel.toggle(it)
                distanceMinLimitInputLayout.toggle(it)
                distanceMaxLimitInputLayout.toggle(it)
                distanceRangeSliderDivider.toggle(it)
                limitsRangeDivider.toggle(it)
                sortByDistance.toggle(it)
            }
        }
        viewModel.sortOption.observe(viewLifecycleOwner) {
            when (it) {
                SortOption.PRICE -> binding.sortByPrice.isChecked = true
                SortOption.DISTANCE -> binding.sortByDistance.isChecked = true
            }
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
        viewModel.initialDistanceMinLimit.observe(viewLifecycleOwner) {
            binding.distanceMinLimitEditText.setTextSilently(minDistanceTextWatcher, it.toString())
        }
        viewModel.initialDistanceMaxLimit.observe(viewLifecycleOwner) {
            binding.distanceMaxLimitEditText.setTextSilently(maxDistanceTextWatcher, it.toString())
        }
        binding.resetFilters.setOnClickListener {
            viewModel.resetFilter()
        }
        binding.applyFilterButton.setOnClickListener {
            viewModel.applyFilter()
        }
        binding.distanceMinLimitEditText.actionDoneListener {
            hideKeyboard()
            binding.distanceMinLimitEditText.clearFocus()
        }
        binding.distanceMaxLimitEditText.actionDoneListener {
            hideKeyboard()
            binding.distanceMaxLimitEditText.clearFocus()
        }
        return binding.root
    }

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

    private fun hideKeyboard() = activity?.currentFocus?.let { focus ->
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(focus.windowToken, 0)
    }
}