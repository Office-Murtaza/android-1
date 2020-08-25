package com.app.belcobtm.ui.main.coins.send_gift

import android.content.Context
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.View
import androidx.core.os.bundleOf
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.ui.SmsDialogFragment
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
import kotlinx.android.synthetic.main.activity_send_gift.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SendGiftFragment : BaseFragment(), GiphyDialogFragment.GifSelectionListener {
    private val viewModel: SendGiftViewModel by viewModel {
        parametersOf(SendGiftFragmentArgs.fromBundle(requireArguments()).coinCode)
    }
    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.createInstance(requireContext()) }
    private var gifMedia: Media? = null
    private var smsCode: String = ""
    private lateinit var gifsDialog: GiphyDialogFragment

    override val resourceLayout: Int = R.layout.activity_send_gift
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val isMenuEnabled: Boolean = false
    override val retryListener: View.OnClickListener = View.OnClickListener {
        if (viewModel.transactionHash.isBlank()) {
            viewModel.createTransaction(
                amountCryptoView.getDouble(),
                phoneContainerView.getString(),
                messageView.getString(),
                gifMedia?.id ?: ""
            )
        } else {
            viewModel.completeTransaction(smsCode)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GiphyCoreUI.configure(context, GIPHY_API_KEY)
    }

    override fun initViews() {
        amountCryptoView.hint = getString(R.string.send_gift_screen_crypto_amount, viewModel.getCoinCode())
        setToolbarTitle(getString(R.string.send_gift_screen_title, viewModel.getCoinCode()))
        val settings = GPHSettings(
            gridType = GridType.waterfall,
            theme = LightTheme,
            dimBackground = true,
            mediaTypeConfig = arrayOf(GPHContentType.gif)
        )
        gifsDialog = GiphyDialogFragment.newInstance(settings)
        initPrice()
        initBalance()
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
                openGify()
                gifView.show()
            }

        }
        amountCryptoView?.editText?.addTextChangedListener(doubleTextWatcher.firstTextWatcher)
        amountCryptoView.actionDoneListener { validateAndSubmit() }
        nextButtonView.setOnClickListener { validateAndSubmit() }
    }

    override fun initObservers() {
        viewModel.transactionLiveData.listen({
            when (it) {
                SendGiftViewModel.SendGiftTransactionType.CREATE -> showSmsDialog()
                SendGiftViewModel.SendGiftTransactionType.COMPLETE -> popBackStack()
            }
        })
    }

    private fun initPrice() {
        priceUsdView.text = getString(R.string.transaction_price_usd, viewModel.getUsdPrice().toStringUsd())
    }

    private fun initBalance() {
        balanceCryptoView.text =
            getString(
                R.string.transaction_crypto_balance,
                viewModel.getCoinBalance().toStringCoin(),
                viewModel.getCoinCode()
            )
        balanceUsdView.text = getString(R.string.transaction_price_usd, viewModel.getUsdBalance().toStringUsd())
    }

    private fun selectMaxPrice() = amountCryptoView.setText(viewModel.getMaxPrice().toStringCoin())

    private fun openGify() {
        gifsDialog.show(childFragmentManager, "gifs_dialog")
        gifsDialog.gifSelectionListener = this
    }

    private fun validateAndSubmit() {
        amountCryptoView.error = null
        phoneContainerView.error = null

        val phoneStrng = phoneContainerView
            .getString()
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")

        var errors = 0

        //Validate amount
        if (amountCryptoView.getDouble() <= 0) {
            errors++
            amountCryptoView.error = getString(R.string.should_be_filled)
        }

        //Validate max amount
        if (amountCryptoView.getDouble() > (viewModel.getCoinBalance() - viewModel.getTransactionFee())) {
            errors++
            amountCryptoView.error = "Not enough balance"
        }

        //Validate amount
        if (!isValidMobileNumber(phoneStrng)) {
            errors++
            showError("Wrong phone number")
        }

        if (errors == 0) {
            viewModel.createTransaction(
                amountCryptoView.getDouble(),
                phoneStrng,
                messageView.getString(),
                gifMedia?.id ?: ""
            )
        }
    }

    private fun showSmsDialog(errorMessage: String? = null) {
        val fragment = SmsDialogFragment()
        fragment.arguments = bundleOf(SmsDialogFragment.TAG_ERROR to errorMessage)
        fragment.show(childFragmentManager, SmsDialogFragment::class.simpleName)
        fragment.setDialogListener { smsCode ->
            this.smsCode = smsCode
            viewModel.completeTransaction(smsCode)
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
    }

    private val doubleTextWatcher: DoubleTextWatcher = DoubleTextWatcher(
        firstTextWatcher = { editable ->
            val fromMaxValue = viewModel.getCoinBalance() - viewModel.getTransactionFee()
            val fromCoinAmountTemporary: Double =
                if (editable.getDouble() > fromMaxValue) fromMaxValue
                else editable.getDouble()
            val toCoinAmount = fromCoinAmountTemporary * viewModel.getUsdPrice()

            if (fromCoinAmountTemporary > fromMaxValue) {
                editable.clear()
                editable.insert(0, fromMaxValue.toStringCoin())
            }

            if (fromCoinAmountTemporary > 0) {
                amountUsdView.text = getString(R.string.unit_usd_dynamic_symbol, toCoinAmount.toStringUsd())
            } else {
                amountUsdView.text = getString(R.string.unit_usd_dynamic_symbol, "0.0")
            }
            updateNextButton()
        }
    )
}
