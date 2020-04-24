package com.app.belcobtm.presentation.features.wallet.deposit

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewTreeObserver
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.QRUtils
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import kotlinx.android.synthetic.main.activity_deposit.*

class DepositActivity : BaseActivity() {
    private lateinit var coinItem: IntentCoinItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)
        initListeners()
        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initListeners() {
        copyButtonView.setOnClickListener {
            copyToClipboard(getString(R.string.wallet_code_clipboard), coinItem.publicKey)
            AlertHelper.showToastLong(applicationContext, R.string.deposit_screen_wallet_copied)
        }
        imageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = imageView.layoutParams
                val imageSize = imageView.width
                params.height = imageSize
                imageView.layoutParams = params
                imageView.setImageBitmap(QRUtils.getSpacelessQR(coinItem.publicKey, imageSize, imageSize))
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun initViews() {
        coinItem = intent.getParcelableExtra(TAG_COIN_ITEM)!!
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.deposit_screen_title, coinItem.coinCode)
        addressView.text = coinItem.publicKey
    }

    private fun copyToClipboard(toastText: String, copiedText: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(toastText, copiedText)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        const val TAG_COIN_ITEM = "tag_coin_item"
    }
}