package com.app.belcobtm.presentation.features.wallet.send.gift

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentSendGiftBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.Const.GIPHY_API_KEY
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
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
import org.koin.android.viewmodel.ext.android.viewModel

class SendGiftFragment : BaseFragment<FragmentSendGiftBinding>(),
    GiphyDialogFragment.GifSelectionListener {

    private val viewModel: SendGiftViewModel by viewModel()
    private val sendGiftArgs: SendGiftFragmentArgs by navArgs()
    private lateinit var gifsDialog: GiphyDialogFragment
    private var gifMedia: Media? = null

    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        if (viewModel.initialLoadingData.value is LoadingData.Error) {
            viewModel.fetchInitialData()
        } else {
            viewModel.sendGift(
                binding.sendCoinInputLayout.getEditText().text.getDouble(),
                sendGiftArgs.phoneNumber,
                binding.messageView.getString(),
                gifMedia?.id
            )
        }
    }

    private val cryptoAmountTextWatcher by lazy {
        SafeDecimalEditTextWatcher { editable ->
            val parsedCoinAmount = editable.getDouble()
            if (parsedCoinAmount != viewModel.sendCoinAmount.value) {
                viewModel.updateAmountToSend(parsedCoinAmount)
            }
            if (editable.isEmpty()) {
                editable.insert(0, "0")
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GiphyCoreUI.configure(context, GIPHY_API_KEY)
    }

    override fun FragmentSendGiftBinding.initViews() {
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
        sendCoinInputLayout.getEditText().setText("0")
    }

    override fun FragmentSendGiftBinding.initListeners() {
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
                messageView.editText?.clearFocus()
            }
            hideKeyboard()
        }
        sendCoinInputLayout.getEditText().addTextChangedListener(cryptoAmountTextWatcher)
        sendGift.setOnClickListener { sendGift() }
    }

    override fun FragmentSendGiftBinding.initObservers() {
        viewModel.initialLoadingData.listen({})
        viewModel.sendCoinAmount.observe(viewLifecycleOwner) { cryptoAmount ->
            with(sendCoinInputLayout.getEditText()) {
                if (text.getDouble() == 0.0 && cryptoAmount == 0.0) {
                    return@observe
                }
                removeTextChangedListener(cryptoAmountTextWatcher)
                setText(cryptoAmount.toStringCoin())
                if (isFocused) {
                    setSelection(text.length)
                }
                addTextChangedListener(cryptoAmountTextWatcher)
            }
        }
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            binding.sendCoinInputLayout.setErrorText(it?.let(::getString), true)
        }
        viewModel.usdAmount.observe(viewLifecycleOwner) {
            binding.amountUsdView.text = getString(R.string.text_usd, it)
        }
        viewModel.coinToSend.observe(viewLifecycleOwner) {
            setCoinData()
        }
        viewModel.fee.observe(viewLifecycleOwner) {
            setCoinData()
        }
        viewModel.sendGiftLoadingData.listen(
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
                        binding.sendCoinInputLayout.setErrorText(
                            getString(R.string.error_xrp_amount_is_not_enough), true
                        )
                        showContent()
                    }
                    else -> showErrorSomethingWrong()
                }
            })
    }

    private fun setCoinData() {
        val coin = viewModel.coinToSend.value ?: return
        val fee = viewModel.fee.value ?: return
        val coinCode = coin.code
        val coinBalance = coin.balanceCoin.toStringCoin()
        val localType = LocalCoinType.valueOf(coinCode)
        binding.sendCoinInputLayout.setCoinData(coinCode, localType.resIcon())
        binding.sendCoinInputLayout.setHelperText(
            getString(
                R.string.send_gift_screen_balance_formatted,
                coinBalance,
                coinCode,
                fee.toStringCoin(),
                coinCode
            )
        )
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
        binding.gifImage.setMedia(media, RenditionType.original)
        binding.gifImage.show()
        binding.removeGifButton.show()
    }

    private fun sendGift() {
        val phone = sendGiftArgs.phoneNumber
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")
        viewModel.sendGift(
            binding.sendCoinInputLayout.getEditText().text.getDouble(),
            phone,
            binding.messageView.getString(),
            gifMedia?.id ?: ""
        )
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSendGiftBinding =
        FragmentSendGiftBinding.inflate(inflater, container, false)
}
