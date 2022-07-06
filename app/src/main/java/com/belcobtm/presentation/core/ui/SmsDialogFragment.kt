package com.belcobtm.presentation.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.belcobtm.R
import com.belcobtm.databinding.FragmentDialogSmsCodeBinding
import com.belcobtm.presentation.tools.extensions.getString
import com.belcobtm.presentation.tools.extensions.showError

class SmsDialogFragment : DialogFragment() {
    private var listener: ((smsCode: String) -> Unit)? = null
    private lateinit var binding: FragmentDialogSmsCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setCanceledOnTouchOutside(false)
        binding = FragmentDialogSmsCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.smsCodeView.error = arguments?.getString(TAG_ERROR)
        arguments?.remove(TAG_ERROR)

        binding.nextButtonView.setOnClickListener {
            if (binding.smsCodeView.getString().length != SMS_CODE_LENGTH) {
                binding.smsCodeView.showError(R.string.error_sms_code_4_digits)
            } else {
                listener?.invoke(binding.smsCodeView.getString())
                dismiss()
            }
        }
        binding.cancelButtonView.setOnClickListener { dismiss() }
    }

    fun setDialogListener(listener: (smsCode: String) -> Unit) {
        this.listener = listener
    }

    companion object {
        private const val SMS_CODE_LENGTH = 4
        const val TAG_ERROR = "sms_dialog_tag_error"
    }
}