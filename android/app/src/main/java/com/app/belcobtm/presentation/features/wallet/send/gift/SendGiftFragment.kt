package com.app.belcobtm.presentation.features.wallet.send.gift

import android.content.Context
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.watcher.DoubleTextWatcher
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.enums.RenditionType
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.GiphyCoreUI
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.themes.LightTheme
import com.giphy.sdk.ui.views.GiphyDialogFragment
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.android.synthetic.main.fragment_send_gift.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SendGiftFragment : BaseFragment(), GiphyDialogFragment.GifSelectionListener {
    private val viewModel: SendGiftViewModel by viewModel {
        parametersOf(SendGiftFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.createInstance(requireContext()) }
    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val cryptoAmount: Double = editable.getDouble()
            if (cryptoAmount > 0) {
                amountUsdView.text = getString(
                    R.string.text_usd,
                    (cryptoAmount * viewModel.getUsdPrice()).toStringUsd()
                )
            } else {
                amountUsdView.text = getString(R.string.text_usd, "0.0")
            }
            updateNextButton()
        }
    )
    private lateinit var gifsDialog: GiphyDialogFragment
    private var gifMedia: Media? = null

    override val resourceLayout: Int = R.layout.fragment_send_gift
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { sendGift() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GiphyCoreUI.configure(context, GIPHY_API_KEY)
    }

    override fun initViews() {
        amountCryptoView.hint = getString(R.string.text_amount, viewModel.getCoinCode())
        setToolbarTitle(getString(R.string.send_gift_screen_title, viewModel.getCoinCode()))
        val settings = GPHSettings(
            gridType = GridType.waterfall,
            theme = LightTheme,
            dimBackground = true,
            mediaTypeConfig = arrayOf(GPHContentType.gif)
        )
        gifsDialog = GiphyDialogFragment.newInstance(settings)
        priceUsdView.text = getString(R.string.text_usd, viewModel.getUsdPrice().toStringUsd())
        balanceCryptoView.text = getString(
            R.string.text_text,
            viewModel.getCoinBalance().toStringCoin(),
            viewModel.getCoinCode()
        )
        balanceUsdView.text = getString(R.string.text_usd, viewModel.getUsdBalance().toStringUsd())
        reservedCryptoView.text = getString(
            R.string.text_text,
            viewModel.getReservedBalanceCoin().toStringCoin(),
            viewModel.getCoinCode()
        )
        reservedUsdView.text = getString(
            R.string.text_usd,
            viewModel.getReservedBalanceUsd().toStringUsd()
        )
        amountCryptoView.helperText = getString(
            R.string.transaction_helper_text_commission,
            viewModel.getTransactionFee().toStringCoin(),
            viewModel.getCoinCode()
        )
    }

    override fun initListeners() {
        phoneContainerView?.editText?.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        phoneContainerView.editText?.afterTextChanged { updateNextButton() }
        maxCryptoView.setOnClickListener { selectMaxPrice() }
        addOrRemoveButtonView.setOnClickListener {
            if (gifMedia != null) {
                gifMedia = null
                gifView.setMedia(null, RenditionType.original)
                gifView.hide()
            } else {
                openGift()
                gifView.show()
            }
        }
        amountCryptoView?.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountCryptoView.actionDoneListener { validateAndSubmit() }
        nextButtonView.setOnClickListener { validateAndSubmit() }
        messageView.editText?.actionDoneListener { if (nextButtonView.isEnabled) sendGift() }
    }

    override fun initObservers() {
        viewModel.sendGiftLiveData.listen(
            success = {
                AlertHelper.showToastShort(requireContext(), R.string.transactions_screen_transaction_created)
                popBackStack()
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showSnackBar(it.message ?: "")
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    is Failure.XRPLowAmountToSend -> {
                        amountCryptoView.showError(R.string.error_xrp_amount_is_not_enough)
                        showContent()
                    }
                    else -> showErrorSomethingWrong()
                }
            })
    }

    private fun selectMaxPrice() = amountCryptoView.setText(viewModel.getMaxValue().toStringCoin())

    private fun openGift() {
        gifsDialog.show(childFragmentManager, "gifs_dialog")
        gifsDialog.gifSelectionListener = this
    }

    private fun validateAndSubmit() {
        amountCryptoView.clearError()
        phoneContainerView.clearError()

        var errors = 0

        val validationResult = viewModel.validateAmount(amountCryptoView.getDouble())
        if (validationResult is ValidationResult.InValid) {
            errors++
            amountCryptoView.showError(validationResult.error)
        }

        if (amountCryptoView.getDouble() < viewModel.getMinValue()) {
            errors++
            amountCryptoView.showError(R.string.balance_amount_too_small)
        }

        if (amountCryptoView.getDouble() >= viewModel.getMaxValue()) {
            errors++
            amountCryptoView.showError(R.string.balance_amount_exceeded)
        }

        if (errors == 0) {
            sendGift()
        }
    }

    override fun onDismissed() = Unit

    override fun onGifSelected(media: Media) {
        gifMedia = media
        gifView.visibility = View.VISIBLE
        gifView.setMedia(media, RenditionType.original)
    }

    private fun isValidMobileNumber(phone: String): Boolean = if (phone.isNotBlank()) {
        try {
            val number = PhoneNumberUtil.createInstance(requireContext()).parse(phone, "")
            phoneUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            false
        }
    } else {
        false
    }

    private fun updateNextButton() {
        nextButtonView.isEnabled = phoneContainerView.getString().isNotEmpty()
                && isValidMobileNumber(phoneContainerView.getString())
                && amountCryptoView.isNotBlank()
                && amountCryptoView.getDouble() > 0
    }

    private fun sendGift() {
        val phone = phoneContainerView
            .getString()
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")
        viewModel.sendGift(
            amountCryptoView.getDouble(),
            phone,
            messageView.getString(),
            gifMedia?.id ?: ""
        )
    }
}
