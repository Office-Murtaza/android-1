package com.app.belcobtm.presentation.features.wallet.trade.mytrade.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.FragmentMyTradeDetailsBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.wallet.trade.list.delegate.TradePaymentOptionDelegate
import org.koin.android.viewmodel.ext.android.viewModel

class MyTradeDetailsFragment : BaseFragment<FragmentMyTradeDetailsBinding>() {

    override var isHomeButtonEnabled: Boolean = true

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

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyTradeDetailsBinding =
        FragmentMyTradeDetailsBinding.inflate(inflater, container, false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        viewModel.fetchTradeDetails(args.tradeId)
        return root
    }

    override fun FragmentMyTradeDetailsBinding.initListeners() {
        editButton.setOnClickListener {
            navigate(MyTradeDetailsFragmentDirections.toEditMyTradeDetails(args.tradeId))
        }
        cancelButton.setOnClickListener {
            viewModel.cancel(args.tradeId)
        }
    }

    override fun FragmentMyTradeDetailsBinding.initViews() {
        setToolbarTitle(R.string.my_trade_details_screen_title)
        paymentOptions.adapter = adapter
    }

    override fun FragmentMyTradeDetailsBinding.initObservers() {
        viewModel.initialLoadingData.listen()
        viewModel.price.observe(viewLifecycleOwner, price::setText)
        viewModel.paymentOptions.observe(viewLifecycleOwner, adapter::update)
        viewModel.ordersCount.observe(viewLifecycleOwner) {
            openOrdersValue.text = it.toString()
        }
        viewModel.terms.observe(viewLifecycleOwner, terms::setText)
        viewModel.amountRange.observe(viewLifecycleOwner, amountRange::setText)
        viewModel.tradeType.observe(viewLifecycleOwner) {
            with(tradeTypeChip) {
                if (it == TradeType.BUY) {
                    setBackgroundResource(R.drawable.trade_type_buy_background)
                    setChipIconResource(R.drawable.ic_trade_type_buy)
                    setText(R.string.trade_type_buy_label)
                    setTextColor(
                        androidx.core.content.ContextCompat.getColor(
                            context,
                            R.color.trade_type_buy_trade_text_color
                        )
                    )
                } else {
                    setBackgroundResource(R.drawable.trade_type_sell_background)
                    setChipIconResource(R.drawable.ic_trade_type_sell)
                    setText(R.string.trade_type_sell_label)
                    setTextColor(
                        androidx.core.content.ContextCompat.getColor(
                            context,
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
                        showSnackBar(it.message.orEmpty())
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    is Failure.ValidationError -> showError(it.message.orEmpty())
                    else -> showErrorSomethingWrong()
                }
            })
    }
}