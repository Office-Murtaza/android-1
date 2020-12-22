package com.app.belcobtm.presentation.features.deals.swap

import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.extensions.getDouble
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.toHtmlSpan
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.app.belcobtm.presentation.features.deals.swap.adapter.CoinDialogAdapter
import kotlinx.android.synthetic.main.fragment_swap.*
import org.koin.android.viewmodel.ext.android.viewModel

class SwapFragment : BaseFragment() {
    private val viewModel: SwapViewModel by viewModel()
    private val textWatcher = DoubleTextWatcher(
        maxCharsAfterDotFirst = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        maxCharsAfterDotSecond = DoubleTextWatcher.MAX_CHARS_AFTER_DOT_CRYPTO,
        firstTextWatcher = { editable ->
            val parsedCoinAmount = editable.getDouble()
            if (parsedCoinAmount != viewModel.sendCoinAmount.value) {
                viewModel.setSendAmount(parsedCoinAmount)
            }
        },
        secondTextWatcher = { editable ->
            val parsedCoinAmount = editable.getDouble()
            if (parsedCoinAmount != viewModel.receiveCoinAmount.value) {
                viewModel.setReceiveAmount(parsedCoinAmount)
            }
        }
    )

    override val resourceLayout: Int = R.layout.fragment_swap
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        if (viewModel.initLoadingData.value is LoadingData.Error) {
            // data not yet initialized
            viewModel.fetchInitialData()
        } else {
            // re submit swap
            viewModel.executeSwap()
        }
    }

    override fun initViews() {
        setToolbarTitle(R.string.swap_screen_title)
        sendCoinInputLayout.getEditText().setHint(R.string.swap_screen_send_hint)
        receiveCoinInputLayout.getEditText().setHint(R.string.swap_screen_receive_hint)
        setTextSilently(sendCoinInputLayout.getEditText(), textWatcher.firstTextWatcher, "0")
        setTextSilently(receiveCoinInputLayout.getEditText(), textWatcher.secondTextWatcher, "0")
    }

    override fun initListeners() {
        nextButtonView.setOnClickListener {
            viewModel.executeSwap()
        }
        viewCircle.setOnClickListener {
            viewModel.changeCoins()
        }
        sendCoinInputLayout.setOnMaxClickListener(View.OnClickListener {
            viewModel.setMaxSendAmount()
        })
        receiveCoinInputLayout.setOnMaxClickListener(View.OnClickListener {
            viewModel.setMaxSendAmount()
        })
        sendCoinInputLayout.setOnCoinButtonClickListener(View.OnClickListener {
            val coinToSend = viewModel.coinToSend.value ?: return@OnClickListener
            val coinToReceive = viewModel.coinToReceive.value ?: return@OnClickListener
            val coinsToExclude = listOf(coinToSend, coinToReceive)
            showSelectCoinDialog(coinsToExclude) { viewModel.setCoinToSend(it) }
        })
        receiveCoinInputLayout.setOnCoinButtonClickListener(View.OnClickListener {
            val coinToSend = viewModel.coinToSend.value ?: return@OnClickListener
            val coinToReceive = viewModel.coinToReceive.value ?: return@OnClickListener
            val coinsToExclude = listOf(coinToSend, coinToReceive)
            showSelectCoinDialog(coinsToExclude) { viewModel.setCoinToReceive(it) }
        })
        sendCoinInputLayout.getEditText().addTextChangedListener(textWatcher.firstTextWatcher)
        receiveCoinInputLayout.getEditText().addTextChangedListener(textWatcher.secondTextWatcher)
    }

    override fun initObservers() {
        viewModel.swapLoadingData.listen(success = {
            AlertHelper.showToastShort(requireContext(), R.string.swap_screen_success_message)
            popBackStack()
        })
        viewModel.initLoadingData.listen(success = {}) // just listen
        viewModel.coinsDetailsLoadingState.listen(success = {}) // just listen
        viewModel.coinToSend.observe(viewLifecycleOwner, Observer { coin ->
            val coinCode = coin.code
            val coinBalance = coin.balanceCoin.toStringCoin()
            val localType = LocalCoinType.valueOf(coinCode)
            sendCoinInputLayout.setCoinData(coinCode, localType.resIcon())
            sendCoinInputLayout.setHelperText(
                getString(
                    R.string.swap_screen_balance_formatted, coinBalance, coinCode
                )
            )
        })
        viewModel.coinToReceive.observe(viewLifecycleOwner, Observer { coin ->
            val coinCode = coin.code
            val coinBalance = coin.balanceCoin.toStringCoin()
            val localType = LocalCoinType.valueOf(coinCode)
            receiveCoinInputLayout.setCoinData(coinCode, localType.resIcon())
            receiveCoinInputLayout.setHelperText(
                getString(
                    R.string.swap_screen_balance_formatted, coinBalance, coinCode
                )
            )
        })
        viewModel.swapRate.observe(viewLifecycleOwner, Observer { rate ->
            rateTextView.text = getString(
                R.string.swap_screen_rate_formatted,
                rate.fromCoinAmount.toString(),
                rate.fromCoinCode,
                rate.swapAmount.toStringCoin(),
                rate.swapCoinCode
            ).toHtmlSpan()
        })
        viewModel.swapFee.observe(viewLifecycleOwner, Observer { fee ->
            platformFeeTextView.text = getString(
                R.string.swap_screen_fee_formatted,
                fee.platformFeePercents.toStringCoin(),
                fee.platformFeeCoinAmount.toStringCoin(),
                fee.swapCoinCode
            ).toHtmlSpan()
        })
        viewModel.coinToSendError.observe(viewLifecycleOwner, Observer { error ->
            sendCoinInputLayout.setErrorText(
                when (error) {
                    is ValidationResult.Valid -> null
                    is ValidationResult.InValid -> getString(error.error)
                }
            )
        })
        viewModel.sendCoinAmount.observe(viewLifecycleOwner, Observer { sendAmount ->
            val targetEditText = sendCoinInputLayout.getEditText()
            if (targetEditText.text.getDouble() == 0.0 && sendAmount == 0.0) {
                return@Observer
            }
            val coinAmountString = sendAmount.toStringCoin()
            val watcher = textWatcher.firstTextWatcher
            setTextSilently(targetEditText, watcher, coinAmountString)
        })
        viewModel.receiveCoinAmount.observe(viewLifecycleOwner, Observer { receiveAmount ->
            val targetEditText = receiveCoinInputLayout.getEditText()
            if (targetEditText.text.getDouble() == 0.0 && receiveAmount == 0.0) {
                return@Observer
            }
            val coinAmountString = receiveAmount.toStringCoin()
            val watcher = textWatcher.secondTextWatcher
            setTextSilently(targetEditText, watcher, coinAmountString)
        })
        viewModel.submitEnabled.observe(viewLifecycleOwner, Observer { enabled ->
            nextButtonView.isEnabled = enabled
        })
    }

    private fun showSelectCoinDialog(
        coinsToExclude: List<CoinDataItem>,
        action: (CoinDataItem) -> Unit
    ) {
        val safeContext = context ?: return
        val coinsList = viewModel.originCoinsData.toMutableList().apply {
            removeAll(coinsToExclude)
        }
        val adapter = CoinDialogAdapter(safeContext, coinsList)
        AlertDialog.Builder(safeContext)
            .setAdapter(adapter) { _, position -> action.invoke(coinsList[position]) }
            .create()
            .show()
    }

    private fun setTextSilently(targetEditText: EditText, watcher: TextWatcher, text: String) {
        targetEditText.removeTextChangedListener(watcher)
        targetEditText.setText(text)
        if (targetEditText.isFocused) {
            targetEditText.setSelection(text.length)
        }
        targetEditText.addTextChangedListener(watcher)
    }
}