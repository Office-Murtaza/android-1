package com.app.belcobtm.ui.auth.seed

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.ui.main.main_activity.MainActivity
import kotlinx.android.synthetic.main.activity_seed_phrase.*


class SeedPhraseActivity : AppCompatActivity() {

    private lateinit var mSeedPhrase: String
    private var mFromSettings: Boolean = false

    companion object {
        private const val KEY_SEED = "KEY_SEED"
        private const val KEY_FROM_SETTINGS = "KEY_FROM_SETTINGS"

        fun startActivity(context: Context, seed: String?, fromSettings: Boolean = false) {
            val intent = Intent(context, SeedPhraseActivity::class.java)
            intent.putExtra(KEY_SEED, seed)
            intent.putExtra(KEY_FROM_SETTINGS, fromSettings)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seed_phrase)

        mSeedPhrase = intent.getStringExtra(KEY_SEED)
        mFromSettings = intent.getBooleanExtra(KEY_FROM_SETTINGS, false)
        initView()
    }

    private fun initView() {
        val seedArray = mSeedPhrase.split(" ")
        word_1.text = addColorText(seedArray[0], "1 ")
        word_2.text = addColorText(seedArray[1], "2 ")
        word_3.text = addColorText(seedArray[2], "3 ")
        word_4.text = addColorText(seedArray[3], "4 ")
        word_5.text = addColorText(seedArray[4], "5 ")
        word_6.text = addColorText(seedArray[5], "6 ")
        word_7.text = addColorText(seedArray[6], "7 ")
        word_8.text = addColorText(seedArray[7], "8 ")
        word_9.text = addColorText(seedArray[8], "9 ")
        word_10.text = addColorText(seedArray[9], "10 ")
        word_11.text = addColorText(seedArray[10], "11 ")
        word_12.text = addColorText(seedArray[11], "12 ")

        copy_seed.setOnClickListener {
            copyToClipboard(mSeedPhrase)
            AlertHelper.showToastLong(container.context, R.string.seed_clipboard)
        }
        if (mFromSettings) {
            bt_done.text = "Done"
        }

        bt_done.setOnClickListener {
            if (mFromSettings) {
                onBackPressed()
            } else {
                finishAffinity()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    private fun addColorText(text: String, addingText: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val redSpannable = SpannableString(addingText)
        redSpannable.setSpan(
            ForegroundColorSpan(getColor(R.color.light_gray_text_color)),
            0,
            addingText.length,
            0
        )
        builder.append(redSpannable).append(text)
        return builder
    }

    private fun copyToClipboard(copiedText: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(copiedText, copiedText)
        clipboard.setPrimaryClip(clip)
    }
}
