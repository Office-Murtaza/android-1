package com.belcobtm.presentation.screens.authorization.create.seed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentCreateSeedBinding
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.helper.ClipBoardHelper
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.authorization.recover.seed.SeedWatcher
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment
import com.belcobtm.presentation.screens.sms.code.SmsCodeFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateSeedFragment : BaseFragment<FragmentCreateSeedBinding>() {

    private val viewModel: CreateSeedViewModel by viewModel()
    private val args: CreateSeedFragmentArgs by navArgs()
    private val clipBoardHelper: ClipBoardHelper by inject()
    override val isBackButtonEnabled: Boolean = true
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
            seedView.setText("")
            viewModel.createSeed()
        }
        pasteButtonView.setOnClickListener {
            val seed = clipBoardHelper.getTextFromClipboard()?.takeIf(String::isNotBlank)
            if (seed != null && viewModel.isValidSeed(seed)) {
                seedView.setText("")
                viewModel.saveSeed(seed)
            } else {
                showToast(R.string.seed_pharse_paste_error_message)
            }
        }
    }

    override fun FragmentCreateSeedBinding.initObservers() {
        viewModel.seedLiveData.observe(viewLifecycleOwner) { seedPhrase ->
            if (args.mode != MODE_SETTINGS) {
                showSeed(seedPhrase)
            }
        }
        viewModel.createWalletLiveData.listen({
            navigate(
                R.id.to_pin_code_fragment,
                bundleOf(PinCodeFragment.TAG_PIN_MODE to PinCodeFragment.KEY_PIN_MODE_CREATE)
            )
        })
        viewModel.invalidSeedErrorMessage.observe(viewLifecycleOwner) { messageRes ->
            messageRes?.let {
                showToast(it)
            }
        }
    }

    private fun showSeed(seedPhrase: String) {
        binding.seedView.setText(seedPhrase)
        binding.seedView.setSelection(seedPhrase.length)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.seed?.takeIf { args.mode == MODE_SETTINGS }?.let(::showSeed)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            goBack()
            true
        } else {
            false
        }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateSeedBinding =
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
        clipBoardHelper.setTextToClipboard(copiedText)
        AlertHelper.showToastShort(requireContext(), R.string.copied)
    }

    private fun createWallet() {
        viewModel.createWallet(
            binding.seedView.text.toString(),
            requireArguments().getString(SmsCodeFragment.TAG_PHONE, ""),
            requireArguments().getString(TAG_PASSWORD, ""),
            requireArguments().getString(TAG_EMAIL, "")
        )
    }

    companion object {

        const val MODE_SETTINGS = 1
        const val MODE_DEFAULT = -1
        const val TAG_PASSWORD = "tag_create_seed_password"
        const val TAG_EMAIL = "tag_create_seed_email"
        const val CHAR_NEXT_LINE: String = "\n"
        const val CHAR_SPACE: String = " "
    }

}
