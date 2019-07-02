package com.app.belcobtm.ui.auth.recover_seed

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_recover_seed_phrase.*
import org.jetbrains.anko.design.longSnackbar


class RecoverSeedPhraseActivity : BaseMvpActivity<RecoverSeedContract.View, RecoverSeedContract.Presenter>(),
    RecoverSeedContract.View {

    override var mPresenter: RecoverSeedContract.Presenter = RecoverSeedPresenter()

    private lateinit var mSeedPhrase: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_seed_phrase)



        bt_done.setOnClickListener { container.longSnackbar("Register finished. Main screen in progress...") }//todo open main screen
        paste_seed.setOnClickListener {
            mSeedPhrase = getTextFromClipboard()
            initSeedView()
        }

    }

    override fun onResume() {
        super.onResume()
//        val maxWidth = word_1.width
//        word_1.maxWidth = maxWidth
////        word_1.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
//
//        val params = word_1_container.layoutParams as ConstraintLayout.LayoutParams
//        params.startToStart = -1
//        params.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
//        word_1_container.requestLayout()
//
//        val params2 = word_2_container.layoutParams as ConstraintLayout.LayoutParams
//        params2.startToEnd = -1
//        params2.endToStart = -1
//        params2.startToStart = R.id.card_container
//        params2.endToEnd = R.id.card_container
//        params2.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
//        word_2_container.requestLayout()
//
//        val params3 = word_3_container.layoutParams as ConstraintLayout.LayoutParams
//        params3.endToEnd = -1
//        params3.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
//        word_2_container.requestLayout()
    }

    private fun initSeedView() {
        try {
            val seedArray = mSeedPhrase.split(" ")
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
            word_13.setText(seedArray[12])
            word_14.setText(seedArray[13])
            word_15.setText(seedArray[14])
        } catch (e: Exception) {
            showError(e.message)
        }


    }

    override fun showProgress(show: Boolean) {
        runOnUiThread {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
        }
    }

    private fun getTextFromClipboard(): String {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        return item?.text.toString()
    }


    override fun openSmsCodeDialog(error: String?) {
        val view = layoutInflater.inflate(com.app.belcobtm.R.layout.view_sms_code_dialog, null)
        val smsCode = view.findViewById<AppCompatEditText>(com.app.belcobtm.R.id.sms_code)
        AlertDialog
            .Builder(this)
            .setTitle(getString(com.app.belcobtm.R.string.verify_sms_code))
            .setPositiveButton(com.app.belcobtm.R.string.next)
            { _, _ ->
                val code = smsCode.text.toString()
                if (code.length != 4) {
                    openSmsCodeDialog(getString(com.app.belcobtm.R.string.error_sms_code_4_digits))
                } else {
                    mPresenter.verifyCode(code)
                }
            }
            .setNegativeButton(com.app.belcobtm.R.string.cancel) { _, _ -> onBackPressed() }
            .setView(view)
            .create()
            .show()
        val tilSmsCode = view.findViewById<TextInputLayout>(com.app.belcobtm.R.id.til_sms_code)
        tilSmsCode.error = error
    }
}
