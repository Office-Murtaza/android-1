package com.app.belcobtm.presentation.features.authorization.recover.seed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentRecoverSeedBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.authorization.interactor.*
import com.app.belcobtm.presentation.core.extensions.clearError
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.extensions.showError
import com.app.belcobtm.presentation.core.helper.ClipBoardHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class RecoverSeedFragment : BaseFragment<FragmentRecoverSeedBinding>() {
    private val viewModel: RecoverSeedViewModel by viewModel()
    private var watcher: SeedWatcher? = null
    private val clipBoardHelper: ClipBoardHelper by inject()
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val backPressedListener: View.OnClickListener =
        View.OnClickListener { popBackStack(R.id.recover_wallet_fragment, false) }
    override val retryListener: View.OnClickListener = View.OnClickListener { recoverWallet() }

    override fun FragmentRecoverSeedBinding.initViews() {
        setToolbarTitle(R.string.recover_seed_screen_title)
    }


    override fun FragmentRecoverSeedBinding.initObservers() {
        viewModel.recoverWalletLiveData.listen(
            success = {
                navigate(
                    R.id.to_pin_code_fragment,
                    bundleOf(PinCodeFragment.TAG_PIN_MODE to PinCodeFragment.KEY_PIN_MODE_CREATE)
                )
            },
            error = {
                when ((it as? Failure.MessageError)?.code) {
                    RECOVER_ERROR_EMPTY_COINS -> {
                        //show common error for now. It's impossible to get it because coins are hardcoded
                        showErrorServerError()
                    }
                    RECOVER_ERROR_MISSED_COINS -> {
                        //show common error for now. It's impossible to get it because coins are hardcoded
                        showErrorServerError()
                    }
                    RECOVER_ERROR_PHONE_DOESNT_EXISTS -> {
                        //nothing to do because we check pass on previous step
                    }
                    RECOVER_ERROR_INCORRECT_PASSWORD -> {
                        //nothing to do because we check pass on previous step
                    }
                    RECOVER_ERROR_SEED_PHRASE -> {
                        seedContainerView.showError(R.string.recover_seed_screen_incorrect_phrase)
                        showContent()
                    }
                    else -> baseErrorHandler(it)
                }
            }
        )
    }

    @SuppressLint("SetTextI18n")
    override fun FragmentRecoverSeedBinding.initListeners() {
        watcher = SeedWatcher(requireContext())
        seedView.addTextChangedListener(watcher)
        nextButtonView.setOnClickListener { recoverWallet() }
        pasteButtonView.setOnClickListener {
            val text = clipBoardHelper.getTextFromClipboard().orEmpty()
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
        val wordList: List<String> = binding.seedView.getString()
            .replace(SeedWatcher.CHAR_NEXT_LINE, SeedWatcher.CHAR_SPACE)
            .splitToSequence(SeedWatcher.CHAR_SPACE)
            .filter { it.isNotEmpty() }
            .toList()

        binding.seedContainerView.clearError()
        if (wordList.size == SEED_PHRASE_WORDS_SIZE) {
            val seed = wordList.joinToString(separator = " ")
            val phone = requireArguments().getString(SmsCodeFragment.TAG_PHONE, "")
            val password = requireArguments().getString(TAG_PASSWORD, "")
            viewModel.recoverWallet(seed, phone, password)
        } else {
            binding.seedContainerView.showError(R.string.recover_seed_screen_error_length)
        }
    }

    companion object {
        const val TAG_PASSWORD = "tag_recover_seed_password"
        const val SEED_PHRASE_WORDS_SIZE = 12
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecoverSeedBinding =
        FragmentRecoverSeedBinding.inflate(inflater, container, false)
}
