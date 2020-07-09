package com.app.belcobtm.presentation.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.extensions.showError
import kotlinx.android.synthetic.main.fragment_dialog_sms_code.*

class SmsDialogFragment : DialogFragment() {
    private var listener: ((smsCode: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setCanceledOnTouchOutside(false)
        return inflater.inflate(R.layout.fragment_dialog_sms_code, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        smsCodeView.error = arguments?.getString(TAG_ERROR)
        arguments?.remove(TAG_ERROR)

        nextButtonView.setOnClickListener {
            if (smsCodeView.getString().length != SMS_CODE_LENGTH) {
                smsCodeView.showError(R.string.error_sms_code_4_digits)
            } else {
                listener?.invoke(smsCodeView.getString())
                dismiss()
            }
        }
        cancelButtonView.setOnClickListener { dismiss() }
    }

    fun setDialogListener(listener: (smsCode: String) -> Unit) {
        this.listener = listener
    }

    companion object {
        private const val SMS_CODE_LENGTH = 4
        const val TAG_ERROR = "sms_dialog_tag_error"
    }
}