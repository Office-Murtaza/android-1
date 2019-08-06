package com.app.belcobtm.ui.main.settings.check_pass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.auth.seed.SeedPhraseActivity
import com.app.belcobtm.ui.main.coins.settings.check_pass.CheckPassContract
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_check_pass.*
import kotlinx.android.synthetic.main.activity_unlink.toolbar
import org.jetbrains.anko.toast

class CheckPassActivity : BaseMvpActivity<CheckPassContract.View, CheckPassContract.Presenter>(),
    CheckPassContract.View {

    companion object {
        private const val KEY_MODE = "KEY_MODE"

        fun start(context: Context?, mode: Mode) {
            val intent = Intent(context, CheckPassActivity::class.java)
            intent.putExtra(KEY_MODE, mode.ordinal)
            context?.startActivity(intent)
        }

        enum class Mode {
            MODE_OPEN_SEED,
            MODE_OPEN_PHONE,
            MODE_CHANGE_PHONE,
            MODE_UNLINK;

            companion object {
                fun valueOfInt(index: Int): Mode {
                    return values()[index]
                }
            }
        }
    }

    private lateinit var mMode: Mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_pass)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mMode = Mode.valueOfInt(
            intent.getIntExtra(
                KEY_MODE,
                0
            )
        )

        when (mMode) {
            Companion.Mode.MODE_OPEN_PHONE -> {
                supportActionBar?.title = getString(R.string.change_phone)
                next.text = getString(R.string.next)
                next.setOnClickListener {
                    mPresenter.checkPass(edit_text.text.toString())
                }
            }
            Companion.Mode.MODE_CHANGE_PHONE -> {
                supportActionBar?.title = getString(R.string.change_phone)
                next.text = getString(R.string.change_phone)
                next.setOnClickListener {
                    mPresenter.updatePhone(edit_text.text.toString())
                }
                til_edit_text.hint = getString(R.string.new_phone_number)
                edit_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_phone, 0)
                //todo add ccp and phone formatting
                //todo change edit_text inputType

            }
            Companion.Mode.MODE_UNLINK -> {
                supportActionBar?.title = getString(R.string.settings_unlink)
                next.text = getString(R.string.settings_unlink)
                next.setOnClickListener {
                    mPresenter.checkPass(edit_text.text.toString())
                }
            }
            Companion.Mode.MODE_OPEN_SEED -> {
                supportActionBar?.title = getString(R.string.open_seed)
                next.text = getString(R.string.open_seed)
                next.setOnClickListener {
                    mPresenter.checkPass(edit_text.text.toString())
                }
            }
        }

        cancel.setOnClickListener { onBackPressed() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPassConfirmed() {
        when (mMode) {
            Companion.Mode.MODE_OPEN_PHONE -> {
                start(this, Mode.MODE_CHANGE_PHONE)
                finish()
            }
            Companion.Mode.MODE_UNLINK -> {
                //todo
                toast("in progress")
            }
            Companion.Mode.MODE_OPEN_SEED -> {
                mPresenter.requestSeed()
            }
        }
    }

    override fun openSmsCodeDialog(error: String?) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        val smsCode = view.findViewById<AppCompatEditText>(R.id.sms_code)
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next)
            { _, _ ->
                val code = smsCode.text.toString()
                if (code.length != 4) {
                    openSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    mPresenter.confirmPhoneSms(edit_text.text.toString(), code)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> onBackPressed() }
            .setView(view)
            .create()
            .show()
        val tilSmsCode = view.findViewById<TextInputLayout>(R.id.til_sms_code)
        tilSmsCode.error = error
    }

    override fun onSmsConfirmed() {
        toast("Phone changed")
        finish()
    }

    override fun onSeedReceived(seed: String?) {
        SeedPhraseActivity.startActivity(this, seed, true)
        finish()
    }

}
