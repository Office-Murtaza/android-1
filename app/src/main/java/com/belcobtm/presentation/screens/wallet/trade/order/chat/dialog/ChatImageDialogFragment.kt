package com.belcobtm.presentation.screens.wallet.trade.order.chat.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.DialogFragmentChatImageBinding
import com.bumptech.glide.Glide

class ChatImageDialogFragment : DialogFragment() {

    private val args by navArgs<ChatImageDialogFragmentArgs>()

    override fun getTheme(): Int = R.style.DialogTransparent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogFragmentChatImageBinding.inflate(inflater, container, false)
        binding.root.setOnClickListener {
            dismiss()
        }
        Glide.with(binding.root)
            .load(args.imageUrl)
            .into(binding.image)
        return binding.root
    }
}