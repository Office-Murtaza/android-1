package com.app.belcobtm.presentation.features.wallet.trade.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentCreateTradeBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.extensions.getDouble
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.app.belcobtm.presentation.features.deals.swap.adapter.CoinDialogAdapter
import com.google.android.material.chip.Chip
import org.koin.android.viewmodel.ext.android.viewModel

class CreateTradeFragment : BaseFragment<FragmentCreateTradeBinding>() {

    override val isHomeButtonEnabled: Boolean
        get() = true
    override val isToolbarEnabled: Boolean
        get() = true

    private val viewModel by viewModel<CreateTradeViewModel>()

    private val cryptoAmountTextWatcher by lazy {
        SafeDecimalEditTextWatcher { editable ->
            val parsedCoinAmount = editable.getDouble()
            if (parsedCoinAmount != viewModel.price.value) {
                viewModel.updatePrice(parsedCoinAmount)
            }
            if (editable.isEmpty()) {
                editable.insert(0, "0")
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateTradeBinding =
        FragmentCreateTradeBinding.inflate(inflater, container, false)

    override fun FragmentCreateTradeBinding.initViews() {
        setToolbarTitle(R.string.create_trade_screen_title)
        coinDetailsView.setMaxButtonEnabled(false)
        coinDetailsView.setErrorEnabled(false)
        coinDetailsView.getEditText().setText("0")
        coinDetailsView.setPadding(0, 0, 0, 0)
    }

    override fun FragmentCreateTradeBinding.initObservers() {
        viewModel.selectedCoin.observe(viewLifecycleOwner, ::setCoinData)
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it?.let(::getString), true)
        }
        viewModel.initialLoadingData.listen({})
        viewModel.price.observe(viewLifecycleOwner) { price ->
            with(coinDetailsView.getEditText()) {
                if (text.getDouble() == 0.0 && price == 0.0) {
                    return@observe
                }
                removeTextChangedListener(cryptoAmountTextWatcher)
                setText(price.toStringCoin())
                if (isFocused) {
                    setSelection(text.length)
                }
                addTextChangedListener(cryptoAmountTextWatcher)
            }
        }
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            coinDetailsView.setErrorText(it?.let(::getString), true)
        }
        viewModel.createTradeLoadingData.listen(
            success = {
                AlertHelper.showToastShort(
                    requireContext(),
                    R.string.transactions_screen_transaction_created
                )
                popBackStack(R.id.fragment_deals, true)
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showSnackBar(it.message.orEmpty())
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    is Failure.XRPLowAmountToSend -> {
                        coinDetailsView.setErrorText(
                            getString(R.string.error_xrp_amount_is_not_enough), true
                        )
                        showContent()
                    }
                    else -> showErrorSomethingWrong()
                }
            })
    }

    override fun FragmentCreateTradeBinding.initListeners() {
        setupTradeTypeCheckChangeListener(tradeTypeBuyChip)
        setupTradeTypeCheckChangeListener(tradeTypeSellChip)
        coinDetailsView.getEditText().addTextChangedListener(cryptoAmountTextWatcher)
        coinDetailsView.setOnCoinButtonClickListener(View.OnClickListener {
            showSelectCoinDialog {
                viewModel.selectCoin(it)
            }
        })
    }

    private fun setCoinData(coin: CoinDataItem) {
        val coinCode = coin.code
        val coinBalance = coin.balanceCoin.toStringCoin()
        val localType = LocalCoinType.valueOf(coinCode)
        binding.coinDetailsView.setCoinData(coinCode, localType.resIcon())
        binding.coinDetailsView.setHelperText(getString(R.string.trade_create_balance_formatted, coinBalance, coinCode))
    }

    private fun showSelectCoinDialog(
        action: (CoinDataItem) -> Unit
    ) {
        val safeContext = context ?: return
        val coinsList = viewModel.getCoinsToSelect()
        val adapter = CoinDialogAdapter(safeContext, coinsList)
        AlertDialog.Builder(safeContext)
            .setAdapter(adapter) { _, position -> action.invoke(coinsList[position]) }
            .create()
            .show()
    }

    private fun setupTradeTypeCheckChangeListener(chip: Chip) {
        chip.setOnCheckedChangeListener { _, isChecked ->
            chip.chipStrokeWidth = if (isChecked) {
                resources.getDimensionPixelSize(R.dimen.divider_size).toFloat()
            } else {
                0.0f
            }
        }
    }
}