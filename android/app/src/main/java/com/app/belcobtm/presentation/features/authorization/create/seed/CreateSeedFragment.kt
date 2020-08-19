package com.app.belcobtm.presentation.features.authorization.create.seed

import android.content.ClipData
import android.content.ClipboardManager
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import kotlinx.android.synthetic.main.fragment_create_seed.*
import org.koin.android.viewmodel.ext.android.viewModel

class CreateSeedFragment : BaseFragment() {
    private val viewModel: CreateSeedViewModel by viewModel()
    val args: CreateSeedFragmentArgs by navArgs()
    override val resourceLayout: Int = R.layout.fragment_create_seed
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val backPressedListener: View.OnClickListener = View.OnClickListener {
        goBack()
    }
    override val retryListener: View.OnClickListener = View.OnClickListener { createWallet() }
    private var seedPhrase = ""

    override fun initViews() {
        setToolbarTitle(R.string.create_seed_screen_title)
        initNextButton()
        showBackButton(true)
        viewModel.passArgs(args)
        if (args.mode == MODE_SETTINGS) {
            args.seed?.run {
                showSeed(this)
            }
        }
    }

    override fun initListeners() {
        copyButtonView.setOnClickListener {
            copyToClipboard(seedPhrase)
        }
    }

    override fun initObservers() {
        viewModel.seedLiveData.observe(viewLifecycleOwner, Observer { seedPhrase ->
            if (args.mode != MODE_SETTINGS) {
                showSeed(seedPhrase)
            }
        })
        viewModel.createWalletLiveData.listen({
            navigate(
                R.id.to_pin_code_fragment,
                bundleOf(PinCodeFragment.TAG_PIN_MODE to PinCodeFragment.KEY_PIN_MODE_CREATE)
            )
        })
    }

    private fun showSeed(seedPhrase: String) {
        this.seedPhrase = seedPhrase
        val wordList: List<String> = seedPhrase
            .replace(CHAR_NEXT_LINE, CHAR_SPACE)
            .splitToSequence(CHAR_SPACE)
            .filter { it.isNotEmpty() }
            .toList()

        word1.text = wordList[0]
        word2.text = wordList[1]
        word3.text = wordList[2]
        word4.text = wordList[3]
        word5.text = wordList[4]
        word6.text = wordList[5]
        word7.text = wordList[6]
        word8.text = wordList[7]
        word9.text = wordList[8]
        word10.text = wordList[9]
        word11.text = wordList[10]
        word12.text = wordList[11]
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = if (item.itemId == android.R.id.home) {
        goBack()
        true
    } else {
        false
    }

    private fun goBack() {
        when (args.mode) {
            MODE_SETTINGS -> {
                navigate(CreateSeedFragmentDirections.seedToSettingsFragment(SETTINGS_SECURITY))
            }
            MODE_DEFAULT -> {
                popBackStack(R.id.create_wallet_fragment, false)
            }
        }
    }

    private fun initNextButton() {
        when (args.mode) {
            MODE_SETTINGS -> {
                nextButtonView.text = getString(R.string.done)
                nextButtonView.setOnClickListener {
                    navigate(R.id.seed_to_settings_fragment)
                }
            }
            MODE_DEFAULT -> {
                nextButtonView.setOnClickListener {
                    createWallet()
                }
            }
        }
    }

    private fun copyToClipboard(copiedText: String) {
        val clipboard = requireContext().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(copiedText, copiedText)
        clipboard.setPrimaryClip(clip)
        AlertHelper.showToastShort(requireContext(), R.string.copied)
    }

    private fun createWallet() {
        viewModel.createWallet(
            requireArguments().getString(SmsCodeFragment.TAG_PHONE, ""),
            requireArguments().getString(TAG_PASSWORD, "")
        )
    }

    companion object {
        const val MODE_SETTINGS = 1
        const val MODE_DEFAULT = -1
        const val TAG_PASSWORD = "tag_create_seed_password"
        const val CHAR_NEXT_LINE: String = "\n"
        const val CHAR_SPACE: String = " "
    }
}