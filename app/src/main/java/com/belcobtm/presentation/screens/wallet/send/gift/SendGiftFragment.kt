package com.belcobtm.presentation.screens.wallet.send.gift

import android.Manifest
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentSendGiftBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.core.views.listeners.SafeDecimalEditTextWatcher
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.getDouble
import com.belcobtm.presentation.tools.extensions.getString
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.tools.extensions.toHtmlSpan
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.toggle
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.Formatter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.core.models.enums.RenditionType
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.views.GiphyDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class SendGiftFragment : BaseFragment<FragmentSendGiftBinding>(),
    GiphyDialogFragment.GifSelectionListener {

    private val viewModel: SendGiftViewModel by viewModel()
    private val sendGiftArgs: SendGiftFragmentArgs by navArgs()
    private lateinit var gifsDialog: GiphyDialogFragment
    private var gifMedia: Media? = null

    override val isBackButtonEnabled: Boolean = true
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener {
        when {
            viewModel.initialLoadingData.value is LoadingData.Error ->
                viewModel.fetchInitialData(sendGiftArgs.phoneNumber)
            viewModel.transactionPlanLiveData.value is LoadingData.Error ->
                viewModel.coinToSend.value?.let {
                    viewModel.selectCoin(sendGiftArgs.phoneNumber, it)
                }
            else -> {
                sendGiftFirstWithPermissionCheck()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.fetchInitialData(sendGiftArgs.phoneNumber)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private val currencyFormatter: Formatter<Double> by inject(
        named(CurrencyPriceFormatter.CURRENCY_PRICE_FORMATTER_QUALIFIER)
    )

    private val cryptoAmountTextWatcher by lazy {
        SafeDecimalEditTextWatcher { editable ->
            val parsedCoinAmount = editable.getDouble()
            if (parsedCoinAmount != viewModel.amount.value?.amount) {
                viewModel.setAmount(parsedCoinAmount)
            }
        }
    }

    override fun FragmentSendGiftBinding.initViews() {
        contactName.toggle(!sendGiftArgs.contactName.isNullOrEmpty())
        contactName.text = sendGiftArgs.contactName
        contactPhone.text = sendGiftArgs.phoneNumber
        sendCoinInputLayout.setHint(getString(R.string.text_amount))
        setToolbarTitle(getString(R.string.send_gift_screen_title))
        val settings = GPHSettings(
            gridType = GridType.waterfall,
            theme = GPHTheme.Light,
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
        amountUsdView.text = currencyFormatter.format(0.0)
    }

    override fun onTouchIntercented(ev: MotionEvent) {
        super.onTouchIntercented(ev)
        if (binding.messageView.editText?.isFocused == true && ev.action == MotionEvent.ACTION_UP) {
            val rect = Rect()
            binding.messageView.getGlobalVisibleRect(rect)
            if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                val isMessageEmpty = binding.messageView.editText?.text.isNullOrEmpty()
                binding.messageView.toggle(!isMessageEmpty)
                binding.addMessage.toggle(isMessageEmpty)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun FragmentSendGiftBinding.initListeners() {
        sendCoinInputLayout.setOnMaxClickListener { viewModel.setMaxAmount() }
        addGif.setOnClickListener { openGift() }
        gifImage.setOnClickListener { openGift() }
        removeGifButton.setOnClickListener {
            gifMedia = null
            gifImage.setMedia(null, RenditionType.original)
            gifImage.hide()
            removeGifButton.hide()
        }
        messageView.editText?.actionDoneListener {
            hideKeyboard()
        }
        sendCoinInputLayout.setOnCoinButtonClickListener {
            AlertHelper.showSelectCoinDialog(requireContext(), viewModel.getCoinsToSelect()) {
                viewModel.selectCoin(sendGiftArgs.phoneNumber, it)
            }
        }
        addMessage.setOnClickListener {
            addMessage.hide()
            messageView.show()
            messageView.requestFocus()
            showKeyboard()
        }
        sendCoinInputLayout.getEditText().addTextChangedListener(cryptoAmountTextWatcher)
        sendGift.setOnClickListener { sendGiftWithPermissionCheck() }
        limitDetails.setOnClickListener {
            navigate(SendGiftFragmentDirections.toServiceInfoDialog(ServiceType.TRANSFER))
        }
    }

    override fun FragmentSendGiftBinding.initObservers() {
        viewModel.initialLoadingData.listen()
        viewModel.transactionPlanLiveData.listen()
        viewModel.amount.observe(viewLifecycleOwner) { cryptoAmount ->
            with(sendCoinInputLayout.getEditText()) {
                val coinsAmountText = cryptoAmount.amount.toStringCoin()
                if (cryptoAmount.amount < 0.0 || text.toString() == coinsAmountText) {
                    return@observe
                }
                removeTextChangedListener(cryptoAmountTextWatcher)
                setText(cryptoAmount.amount.toStringCoin())
                if (isFocused) {
                    setSelection(text.length)
                }
                addTextChangedListener(cryptoAmountTextWatcher)
            }
        }
        viewModel.cryptoAmountError.observe(viewLifecycleOwner) {
            sendCoinInputLayout.setErrorText(it?.let(::getString), true)
        }
        viewModel.usdAmount.observe(viewLifecycleOwner) {
            amountUsdView.text = currencyFormatter.format(it)
        }
        viewModel.coinWithFee.observe(viewLifecycleOwner) { (coin, fee) ->
            setCoinData(coin, fee)
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
                    is Failure.LocationError -> {
                        showToast(it.message.orEmpty())
                        showContent()
                    }
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showToast(it.message.orEmpty())
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    else -> showErrorSomethingWrong()
                }
            })
        viewModel.giftFee.observe(viewLifecycleOwner) { fee ->
            platformFeeTextView.text = getString(
                R.string.sell_screen_fee_formatted,
                fee.platformFeePercents.toStringCoin(),
                fee.platformFeeCoinAmount.toStringCoin(),
                fee.swapCoinCode
            ).toHtmlSpan()
        }
    }

    private fun setCoinData(coin: CoinDataItem, fee: Double) {
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
                when {
                    coinCode.isEthRelatedCoinCode() -> LocalCoinType.ETH.name
                    coinCode == LocalCoinType.XRP.name -> getString(
                        R.string.xrp_additional_transaction_comission, LocalCoinType.XRP.name
                    )
                    else -> coinCode
                }
            )
        )
    }

    private fun openGift() {
        gifsDialog.show(childFragmentManager, "gifs_dialog")
        gifsDialog.gifSelectionListener = this
    }

    override fun onDismissed(selectedContentType: GPHContentType) {
        // noop
    }

    override fun didSearchTerm(term: String) {
        // noop
    }

    override fun onGifSelected(
        media: Media,
        searchTerm: String?,
        selectedContentType: GPHContentType
    ) {
        gifMedia = media
        binding.gifImage.setMedia(media, RenditionType.original)
        binding.gifImage.show()
        binding.removeGifButton.show()
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun sendGift() {
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

    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun sendGiftFirst() {
        viewModel.sendGift(
            binding.sendCoinInputLayout.getEditText().text.getDouble(),
            sendGiftArgs.phoneNumber,
            binding.messageView.getString(),
            gifMedia?.id
        )
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun showLocationError() {
        viewModel.showLocationError()
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSendGiftBinding =
        FragmentSendGiftBinding.inflate(inflater, container, false)

}
