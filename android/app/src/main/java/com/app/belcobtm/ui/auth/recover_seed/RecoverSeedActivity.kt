package com.app.belcobtm.ui.auth.recover_seed

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.coins.MainActivity
import kotlinx.android.synthetic.main.activity_recover_seed_phrase.*


class RecoverSeedActivity : BaseMvpActivity<RecoverSeedContract.View, RecoverSeedContract.Presenter>(),
    RecoverSeedContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_seed_phrase)

        bt_done.setOnClickListener {
            val seed = getSeedFormView()
            if (seed.isNotEmpty())
                mPresenter.verifySeed(seed)
            else showError(getString(R.string.enter_seed_first))
        }
        paste_seed.setOnClickListener {
            val seed = getTextFromClipboard()
            initSeedView(seed)
        }
    }

    override fun onSeedVerifyed() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun getSeedFormView(): String {
        var seed = ""
        if (word_1.text!!.isNotEmpty()
            && word_1.text!!.isNotEmpty()
            && word_2.text!!.isNotEmpty()
            && word_3.text!!.isNotEmpty()
            && word_4.text!!.isNotEmpty()
            && word_5.text!!.isNotEmpty()
            && word_6.text!!.isNotEmpty()
            && word_7.text!!.isNotEmpty()
            && word_8.text!!.isNotEmpty()
            && word_9.text!!.isNotEmpty()
            && word_10.text!!.isNotEmpty()
            && word_11.text!!.isNotEmpty()
            && word_12.text!!.isNotEmpty()
        ) {
            seed += word_1.text.toString() + " "
            seed += word_2.text.toString() + " "
            seed += word_3.text.toString() + " "
            seed += word_4.text.toString() + " "
            seed += word_5.text.toString() + " "
            seed += word_6.text.toString() + " "
            seed += word_7.text.toString() + " "
            seed += word_8.text.toString() + " "
            seed += word_9.text.toString() + " "
            seed += word_10.text.toString() + " "
            seed += word_11.text.toString() + " "
            seed += word_12.text.toString()
        }

        return seed
    }

    private fun initSeedView(seed: String) {
        try {
            val seedArray = seed.split(" ")
            word_1.setText(seedArray[0])
            word_2.setText(seedArray[1])
            word_3.setText(seedArray[2])
            word_4.setText(seedArray[3])
            word_5.setText(seedArray[4])
            word_6.setText(seedArray[5])
            word_7.setText(seedArray[6])
            word_8.setText(seedArray[7])
            word_9.setText(seedArray[8])
            word_10.setText(seedArray[9])
            word_11.setText(seedArray[10])
            word_12.setText(seedArray[11])
        } catch (e: Exception) {
            showError(e.message)
        }
    }

    private fun getTextFromClipboard(): String {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text.toString()
    }
}
