package com.belcobtm.presentation.screens.wallet.trade.order.chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentOrderChatBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.MainFragment
import com.belcobtm.presentation.screens.wallet.trade.order.chat.delegate.MyMessageDelegate
import com.belcobtm.presentation.screens.wallet.trade.order.chat.delegate.PartnerMessageDelegate
import com.belcobtm.presentation.tools.extensions.toggle
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrderChatFragment : BaseFragment<FragmentOrderChatBinding>() {

    override var isBackButtonEnabled: Boolean = true
    private val viewModel by viewModel<OrderChatViewModel>()
    private val args by navArgs<OrderChatFragmentArgs>()

    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                viewModel.setAttachment(uri, bitmap)
            }
        }
    }

    private val adapter: MultiTypeAdapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(MyMessageDelegate {
                navigate(OrderChatFragmentDirections.toChatImageDialog(it))
            })
            registerDelegate(PartnerMessageDelegate {
                navigate(OrderChatFragmentDirections.toChatImageDialog(it))
            })
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOrderChatBinding =
        FragmentOrderChatBinding.inflate(inflater, container, false)

    override fun FragmentOrderChatBinding.initViews() {
        setToolbarTitle(args.orderId)
        binding.chatRecyclerView.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateTimestamp()
    }

    override fun FragmentOrderChatBinding.initListeners() {
        binding.sendButton.setOnClickListener {
            viewModel.sendMessage(args.orderId, binding.message.text?.toString().orEmpty())
            binding.message.text = null
        }
        binding.attachmentButton.setOnClickListener {
            openPicker()
        }
        binding.attachmentRemove.setOnClickListener {
            viewModel.setAttachment(null, null)
        }
    }

    override fun FragmentOrderChatBinding.initObservers() {
        viewModel.chatObserverLoadingData.listen(error = {
            Snackbar.make(binding.root, R.string.send_message_error, Snackbar.LENGTH_SHORT).show()
            showContent()
        })
        viewModel.attachmentImage.observe(viewLifecycleOwner) {
            binding.attachment.toggle(it != null)
            binding.attachmentBackground.toggle(it != null)
            binding.attachmentRemove.toggle(it != null)
            binding.attachment.setImageBitmap(it)
        }
        viewModel.chatData(args.orderId).observe(viewLifecycleOwner) {
            adapter.update(it)
            binding.chatRecyclerView.smoothScrollToPosition(it.size)
            viewModel.updateTimestamp()
        }
    }

    private fun openPicker() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        imagePicker.launch(intent)
    }

    override fun onStart() {
        super.onStart()
        // child-fragment's parent is NavHostFragment and NavHostFragment's parent is parent-fragment
        (parentFragment?.parentFragment as MainFragment?)?.toggleBottomNavigation(false)
    }

    override fun onStop() {
        // child-fragment's parent is NavHostFragment and NavHostFragment's parent is parent-fragment
        (parentFragment?.parentFragment as MainFragment?)?.toggleBottomNavigation(true)
        super.onStop()
    }

}
