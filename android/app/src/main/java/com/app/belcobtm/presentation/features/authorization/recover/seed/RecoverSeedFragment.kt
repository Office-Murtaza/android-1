package com.app.belcobtm.presentation.features.authorization.recover.seed

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.clearError
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.extensions.showError
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import kotlinx.android.synthetic.main.fragment_recover_seed.*
import org.koin.android.viewmodel.ext.android.viewModel

class RecoverSeedFragment : BaseFragment() {
    private val viewModel: RecoverSeedViewModel by viewModel()
    private var watcher: RecoverSeedWatcher? = null
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val resourceLayout: Int = R.layout.fragment_recover_seed
    override val backPressedListener: View.OnClickListener =
        View.OnClickListener { popBackStack(R.id.recover_wallet_fragment, false) }
    override val retryListener: View.OnClickListener = View.OnClickListener { recoverWallet() }

    override fun initViews() {
        super.initViews()
        setToolbarTitle(R.string.recover_seed_screen_title)
    }

    override fun initObservers() {
        viewModel.recoverWalletLiveData.listen(
            success = {
                navigate(
                    R.id.to_pin_code_fragment,
                    bundleOf(PinCodeFragment.TAG_PIN_MODE to PinCodeFragment.KEY_PIN_MODE_CREATE)
                )
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.ServerError -> showErrorServerError()
                    is Failure.MessageError -> if (it.message.equals("No value for errorMsg", true)) {
                        seedContainerView.showError(R.string.recover_seed_screen_incorrect_phrase)
                        showContent()
                    } else {
                        showError(it.message ?: "")
                        showContent()
                    }
                    else -> showErrorSomethingWrong()
                }
            }
        )
    }

    @SuppressLint("SetTextI18n")
    override fun initListeners() {
        super.initListeners()
        watcher = RecoverSeedWatcher(requireContext())
        seedView.addTextChangedListener(watcher)
        nextButtonView.setOnClickListener { recoverWallet() }
        pasteButtonView.setOnClickListener {
            val text = getTextFromClipboard()
            seedView.setText("")
            seedView.setText(text)
            seedView.setSelection(text.length)
            seedContainerView.clearError()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = if (item.itemId == android.R.id.home) {
        popBackStack(R.id.recover_wallet_fragment, false)
        true
    } else {
        false
    }

    private fun recoverWallet() {
        val wordList: List<String> = seedView.getString()
            .replace(RecoverSeedWatcher.CHAR_NEXT_LINE, RecoverSeedWatcher.CHAR_SPACE)
            .splitToSequence(RecoverSeedWatcher.CHAR_SPACE)
            .filter { it.isNotEmpty() }
            .toList()

        seedContainerView.clearError()
        if (wordList.size == SEED_PHRASE_WORDS_SIZE) {
            val seed = wordList.joinToString(separator = " ")
            val phone = requireArguments().getString(SmsCodeFragment.TAG_PHONE, "")
            val password = requireArguments().getString(TAG_PASSWORD, "")
            viewModel.recoverWallet(seed, phone, password)
        } else {
            seedContainerView.showError(R.string.recover_seed_screen_error_length)
        }
    }

    private fun getTextFromClipboard(): String {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return if (item?.text.isNullOrBlank()) "" else item?.text.toString() + " "
    }

    companion object {
        const val TAG_PASSWORD = "tag_recover_seed_password"
        const val SEED_PHRASE_WORDS_SIZE = 12
    }
}
