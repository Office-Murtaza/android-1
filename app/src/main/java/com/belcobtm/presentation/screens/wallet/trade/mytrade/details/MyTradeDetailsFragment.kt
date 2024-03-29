package com.belcobtm.presentation.screens.wallet.trade.mytrade.details

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentTradeMyDetailsBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.wallet.trade.list.delegate.TradePaymentOptionDelegate
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.tools.extensions.setDrawableStart
import com.belcobtm.presentation.tools.extensions.toggle
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyTradeDetailsFragment : BaseFragment<FragmentTradeMyDetailsBinding>() {

    override var isBackButtonEnabled: Boolean = true

    private val args by navArgs<MyTradeDetailsFragmentArgs>()
    private val viewModel by viewModel<MyTradeDetailsViewModel>()
    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(TradePaymentOptionDelegate())
        }
    }

    override val retryListener: View.OnClickListener = View.OnClickListener {
        if (viewModel.initialLoadingData.value is LoadingData.Error<Unit>) {
            viewModel.fetchTradeDetails(args.tradeId)
        } else {
            viewModel.cancel(args.tradeId)
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTradeMyDetailsBinding =
        FragmentTradeMyDetailsBinding.inflate(inflater, container, false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        viewModel.fetchTradeDetails(args.tradeId)
        return root
    }

    override fun FragmentTradeMyDetailsBinding.initListeners() {
        editButton.setOnClickListener {
            navigate(MyTradeDetailsFragmentDirections.toEditMyTradeDetails(args.tradeId))
        }
        cancelButton.setOnClickListener {
            viewModel.cancel(args.tradeId)
        }
    }

    override fun FragmentTradeMyDetailsBinding.initViews() {
        setToolbarTitle(R.string.my_trade_details_screen_title)
        paymentOptions.adapter = adapter
    }

    override fun FragmentTradeMyDetailsBinding.initObservers() {
        viewModel.initialLoadingData.listen()
        viewModel.price.observe(viewLifecycleOwner, price::setText)
        viewModel.paymentOptions.observe(viewLifecycleOwner, adapter::update)
        viewModel.ordersCount.observe(viewLifecycleOwner) {
            openOrdersValue.text = it.toString()
        }
        viewModel.terms.observe(viewLifecycleOwner, terms::setText)
        viewModel.amountRange.observe(viewLifecycleOwner, amountRange::setText)
        viewModel.isOutOfStock.observe(viewLifecycleOwner) { isOutOfStock ->
            if (isOutOfStock) {
                with(amountRange) {
                    setTextColor(ContextCompat.getColor(context, R.color.colorError))
                    setTypeface(typeface, Typeface.BOLD)
                }
            } else {
                with(amountRange) {
                    setTextColor(ContextCompat.getColor(context, R.color.black_text_color))
                    setTypeface(typeface, Typeface.NORMAL)
                }
            }
        }
        viewModel.isCancelled.observe(viewLifecycleOwner) { isCancelled ->
            cancelButton.toggle(!isCancelled)
            editButton.toggle(!isCancelled)
        }
        viewModel.tradeType.observe(viewLifecycleOwner) {
            with(tradeType) {
                if (it == TradeType.BUY) {
                    setBackgroundResource(R.drawable.trade_type_buy_background)
                    setDrawableStart(R.drawable.ic_trade_type_buy)
                    setText(R.string.trade_type_buy_label)
                    setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.secondaryGreen
                        )
                    )
                } else {
                    setBackgroundResource(R.drawable.trade_type_sell_background)
                    setDrawableStart(R.drawable.ic_trade_type_sell)
                    setText(R.string.trade_type_sell_label)
                    setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.trade_type_sell_trade_text_color
                        )
                    )
                }
            }
        }
        viewModel.selectedCoin.observe(viewLifecycleOwner) {
            coinIcon.setImageResource(it.resIcon())
            coinLabel.text = it.name
        }
        viewModel.cancelTradeLoadingData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(),
                    R.string.my_trade_details_cancel_trade_success_message
                )
                popBackStack()
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showToast(it.message.orEmpty())
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    is Failure.ValidationError -> showError(it.message.orEmpty())
                    else -> showErrorSomethingWrong()
                }
            })
    }

}
