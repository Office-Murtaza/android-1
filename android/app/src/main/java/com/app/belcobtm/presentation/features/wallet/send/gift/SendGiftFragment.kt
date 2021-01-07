package com.app.belcobtm.presentation.features.wallet.send.gift

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.deals.swap.adapter.CoinDialogAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.enums.RenditionType
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.GiphyCoreUI
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.themes.LightTheme
import com.giphy.sdk.ui.views.GiphyDialogFragment
import kotlinx.android.synthetic.main.fragment_send_gift.*
import org.koin.android.viewmodel.ext.android.viewModel

class SendGiftFragment : BaseFragment(), GiphyDialogFragment.GifSelectionListener {

    private val viewModel: SendGiftViewModel by viewModel()
    private val sendGiftArgs: SendGiftFragmentArgs by navArgs()
    private lateinit var gifsDialog: GiphyDialogFragment
    private var gifMedia: Media? = null

    override val resourceLayout: Int = R.layout.fragment_send_gift
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        if (viewModel.initialLoadingData.value is LoadingData.Error) {
            viewModel.fetchInitialData()
        } else {
            viewModel.sendGift(0.0, sendGiftArgs.phoneNumber, messageView.getString(), gifMedia?.id)
        }
    }

    private val cryptoAmountTextWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun afterTextChanged(editable: Editable?) {
                val parsedCoinAmount = editable?.getDouble() ?: 0.0
                if (parsedCoinAmount != viewModel.sendCoinAmount.value) {
                    viewModel.updateAmountToSend(parsedCoinAmount)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GiphyCoreUI.configure(context, GIPHY_API_KEY)
    }

    override fun initViews() {
        contactName.toggle(!sendGiftArgs.contactName.isNullOrEmpty())
        contactName.text = sendGiftArgs.contactName
        contactPhone.text = sendGiftArgs.phoneNumber
        setToolbarTitle(getString(R.string.send_gift_screen_title))
        val settings = GPHSettings(
            gridType = GridType.waterfall,
            theme = LightTheme,
            dimBackground = true,
            mediaTypeConfig = arrayOf(GPHContentType.gif)
        )
        gifsDialog = GiphyDialogFragment.newInstance(settings)
        Glide.with(contactImage)
            .run {
                if (sendGiftArgs.contactPhotoUri.isNullOrEmpty()) {
                    load(R.drawable.ic_default_contact)
                } else {
                    load(Uri.parse(sendGiftArgs.contactPhotoUri))
                }
            }
            .transform(CircleCrop())
            .apply(RequestOptions().override(contactImage.width, contactImage.height))
            .into(contactImage)
        messageView.editText?.inputType = EditorInfo.IME_ACTION_DONE
    }

    override fun initListeners() {
        sendCoinInputLayout.setOnMaxClickListener(View.OnClickListener { viewModel.setMaxCoinAmount() })
        addGif.setOnClickListener { openGift() }
        gifImage.setOnClickListener { openGift() }
        removeGifButton.setOnClickListener {
            gifMedia = null
            gifImage.setMedia(null, RenditionType.original)
            gifImage.hide()
            removeGifButton.hide()
        }
        sendCoinInputLayout.setOnCoinButtonClickListener(View.OnClickListener {
            showSelectCoinDialog {
                viewModel.selectCoin(it)
            }
        })
        addMessage.setOnClickListener {
            addMessage.hide()
            messageView.show()
            messageView.requestFocus()
            showKeyboard()
        }
        messageView.editText?.actionDoneListener {
            if (messageView.editText?.text.isNullOrEmpty()) {
                messageView.hide()
                addMessage.show()
                messageView?.editText?.clearFocus()
            }
            hideKeyboard()
        }
        sendCoinInputLayout.getEditText().addTextChangedListener(cryptoAmountTextWatcher)
        sendGift.setOnClickListener { sendGift() }
    }

    override fun initObservers() {
        viewModel.initialLoadingData.listen({})
        viewModel.sendCoinAmount.observe(viewLifecycleOwner) { cryptoAmount ->
            with(sendCoinInputLayout.getEditText()) {
                if (text.getDouble() != cryptoAmount) {
                    removeTextChangedListener(cryptoAmountTextWatcher)
                    setText(cryptoAmount.toString())
                    if (isFocused) {
                        setSelection(text.length)
                    }
                    addTextChangedListener(cryptoAmountTextWatcher)
                }
            }
        }
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            sendCoinInputLayout.setErrorText(it?.let(::getString))
        }
        viewModel.usdAmount.observe(viewLifecycleOwner) {
            amountUsdView.text = getString(R.string.text_usd, it)
        }
        viewModel.coinToSend.observe(viewLifecycleOwner) { coin ->
            val coinCode = coin.code
            val coinBalance = coin.balanceCoin.toStringCoin()
            val localType = LocalCoinType.valueOf(coinCode)
            sendCoinInputLayout.setCoinData(coinCode, localType.resIcon())
            sendCoinInputLayout.setHelperText(
                getString(R.string.swap_screen_balance_formatted, coinBalance, coinCode)
            )
        }
        viewModel.fee.observe(viewLifecycleOwner) { amount ->
            sendCoinInputLayout.setAdditionalHelperText(
                getString(
                    R.string.send_gift_screen_fee_formatted,
                    amount.toStringCoin(),
                    viewModel.coinToSend.value?.code.orEmpty()
                )
            )
        }
        viewModel.sendGiftLoadingData.listen(
            success = {
                AlertHelper.showToastShort(requireContext(), R.string.transactions_screen_transaction_created)
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
                        sendCoinInputLayout.setErrorText(getString(R.string.error_xrp_amount_is_not_enough))
                        showContent()
                    }
                    else -> showErrorSomethingWrong()
                }
            })
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

    private fun openGift() {
        gifsDialog.show(childFragmentManager, "gifs_dialog")
        gifsDialog.gifSelectionListener = this
    }

    override fun onDismissed() = Unit

    override fun onGifSelected(media: Media) {
        gifMedia = media
        gifImage.setMedia(media, RenditionType.original)
        gifImage.show()
        removeGifButton.show()
    }

    private fun sendGift() {
        val phone = sendGiftArgs.phoneNumber
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")
        viewModel.sendGift(
            sendCoinInputLayout.getEditText().text.getDouble(),
            phone,
            messageView.getString(),
            gifMedia?.id ?: ""
        )
    }
}
