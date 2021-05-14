package com.app.belcobtm.presentation.features.authorization.create.seed

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentCreateSeedBinding
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.helper.ClipBoardHelper
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.authorization.recover.seed.SeedWatcher
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class CreateSeedFragment : BaseFragment<FragmentCreateSeedBinding>() {
    private val viewModel: CreateSeedViewModel by viewModel()
    private val args: CreateSeedFragmentArgs by navArgs()
    private val clipBoardHelper: ClipBoardHelper by inject()
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val backPressedListener: View.OnClickListener = View.OnClickListener {
        goBack()
    }
    override val retryListener: View.OnClickListener = View.OnClickListener { createWallet() }

    override fun FragmentCreateSeedBinding.initViews() {
        setToolbarTitle(
            if (args.mode == MODE_SETTINGS)
                R.string.show_seed_screen_title
            else
                R.string.create_seed_screen_title
        )
        description.setText(
            if (args.mode == MODE_SETTINGS)
                R.string.show_seed_screen_description
            else
                R.string.create_seed_screen_description
        )
        initNextButton()
        showBackButton(true)
        if (args.mode == MODE_SETTINGS) {
            isMenuEnabled = true
            showBottomMenu()
            generateButtonView.hide()
            pasteButtonView.hide()
        } else {
            generateButtonView.show()
            pasteButtonView.show()
        }
    }

    override fun FragmentCreateSeedBinding.initListeners() {
        seedView.addTextChangedListener(SeedWatcher(requireContext()))
        copyButtonView.setOnClickListener {
            copyToClipboard(seedView.text.toString())
        }
        generateButtonView.setOnClickListener {
            viewModel.createSeed()
        }
        pasteButtonView.setOnClickListener {
            val seed = clipBoardHelper.getTextFromClipboard()?.takeIf(String::isNotBlank)
            if (seed != null && viewModel.isValidSeed(seed)) {
                viewModel.saveSeed(seed)
            } else {
                showSnackBar(R.string.seed_pharse_paste_error_message)
            }
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
        viewModel.invalidSeedErrorMessage.observe(viewLifecycleOwner) { messageRes ->
            messageRes?.let(::showSnackBar)
        }
    }

    private fun showSeed(seedPhrase: String) {
        binding.seedView.setText(seedPhrase)
        binding.seedView.setSelection(seedPhrase.length)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.seed?.takeIf { args.mode == MODE_SETTINGS }?.let(::showSeed)
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
            binding.seedView.text.toString(),
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