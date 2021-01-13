package com.app.belcobtm.presentation.features.authorization.create.seed

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentCreateSeedBinding
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import org.koin.android.viewmodel.ext.android.viewModel

class CreateSeedFragment : BaseFragment<FragmentCreateSeedBinding>() {
    private val viewModel: CreateSeedViewModel by viewModel()
    private val args: CreateSeedFragmentArgs by navArgs()
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val backPressedListener: View.OnClickListener = View.OnClickListener {
        goBack()
    }
    override val retryListener: View.OnClickListener = View.OnClickListener { createWallet() }
    private var seedPhrase = ""

    override fun FragmentCreateSeedBinding.initViews() {
        setToolbarTitle(R.string.create_seed_screen_title)
        initNextButton()
        showBackButton(true)
        if (args.mode == MODE_SETTINGS) {
            isMenuEnabled = true
            showBottomMenu()
            generateButtonView.hide()
            pasteButtonView.hide()
            args.seed?.run {
                showSeed(this)
            }
        } else {
            generateButtonView.show()
            pasteButtonView.show()
        }
    }

    override fun FragmentCreateSeedBinding.initListeners() {
        copyButtonView.setOnClickListener {
            copyToClipboard(seedPhrase)
        }
        generateButtonView.setOnClickListener {
            viewModel.createSeed()
        }
        pasteButtonView.setOnClickListener {
            getTextFromClipboard().takeIf(String::isNotBlank)
                ?.let(::showSeed)
        }
    }

    override fun FragmentCreateSeedBinding.initObservers() {
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

        binding.word1.text = wordList[0]
        binding.word2.text = wordList[1]
        binding.word3.text = wordList[2]
        binding.word4.text = wordList[3]
        binding.word5.text = wordList[4]
        binding.word6.text = wordList[5]
        binding.word7.text = wordList[6]
        binding.word8.text = wordList[7]
        binding.word9.text = wordList[8]
        binding.word10.text = wordList[9]
        binding.word11.text = wordList[10]
        binding.word12.text = wordList[11]
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = if (item.itemId == android.R.id.home) {
        goBack()
        true
    } else {
        false
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCreateSeedBinding =
        FragmentCreateSeedBinding.inflate(inflater, container, false)

    private fun goBack() {
        when (args.mode) {
            MODE_SETTINGS -> {
                popBackStack(R.id.security_fragment, false)
            }
            MODE_DEFAULT -> {
                popBackStack(R.id.create_wallet_fragment, false)
            }
        }
    }

    private fun initNextButton() {
        when (args.mode) {
            MODE_SETTINGS -> {
                binding.nextButtonView.text = getString(R.string.done)
                binding.nextButtonView.setOnClickListener {
                    popBackStack(R.id.security_fragment, false)
                }
            }
            MODE_DEFAULT -> {
                binding.nextButtonView.setOnClickListener {
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

    private fun getTextFromClipboard(): String {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return if (item?.text.isNullOrBlank()) "" else item?.text.toString() + " "
    }

    companion object {
        const val MODE_SETTINGS = 1
        const val MODE_DEFAULT = -1
        const val TAG_PASSWORD = "tag_create_seed_password"
        const val CHAR_NEXT_LINE: String = "\n"
        const val CHAR_SPACE: String = " "
    }
}