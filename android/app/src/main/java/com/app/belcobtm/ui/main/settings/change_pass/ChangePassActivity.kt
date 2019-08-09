package com.app.belcobtm.ui.main.settings.change_pass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.main.coins.settings.change_pass.ChangePassContract
import kotlinx.android.synthetic.main.activity_change_pass.*
import kotlinx.android.synthetic.main.activity_check_pass.*
import kotlinx.android.synthetic.main.activity_check_pass.cancel
import kotlinx.android.synthetic.main.activity_unlink.toolbar
import org.jetbrains.anko.toast


class ChangePassActivity : BaseMvpActivity<ChangePassContract.View, ChangePassContract.Presenter>(),
    ChangePassContract.View {

    companion object {
        private const val KEY_MODE = "KEY_MODE"

        fun start(context: Context?, mode: Mode) {
            val intent = Intent(context, ChangePassActivity::class.java)
            intent.putExtra(KEY_MODE, mode.ordinal)
            context?.startActivity(intent)
        }

        enum class Mode {
            MODE_CHANGE_PASS,
            MODE_CHANGE_PIN;

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
        setContentView(com.app.belcobtm.R.layout.activity_change_pass)
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
            Mode.MODE_CHANGE_PASS -> {
                supportActionBar?.title = getString(com.app.belcobtm.R.string.change_password)
                change_value.setOnClickListener {
                    hideSoftKeyboard()
                    mPresenter.changePass(
                        old_value.text.toString(),
                        new_value.text.toString(),
                        confirm_new_value.text.toString()
                    )
                }
            }
            Mode.MODE_CHANGE_PIN -> {
                supportActionBar?.title = getString(com.app.belcobtm.R.string.change_pin)

                change_value.text = getString(com.app.belcobtm.R.string.change_pin)
                til_old_pass.hint = getString(com.app.belcobtm.R.string.old_pin)
                til_new_pass.hint = getString(com.app.belcobtm.R.string.new_pin)
                til_confirm_new_pass.hint = getString(com.app.belcobtm.R.string.confirm_new_pin)

                old_value.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                new_value.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                confirm_new_value.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

                val filterArray = arrayOfNulls<InputFilter>(1)
                filterArray[0] = InputFilter.LengthFilter(6)
                old_value.filters = filterArray
                new_value.filters = filterArray
                confirm_new_value.filters = filterArray


                change_value.setOnClickListener {
                    hideSoftKeyboard()
                    mPresenter.changePin(
                        old_value.text.toString(),
                        new_value.text.toString(),
                        confirm_new_value.text.toString()
                    )
                }
            }
        }

        confirm_new_value.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                hideSoftKeyboard()
                change_value.performClick()
                return@OnEditorActionListener true
            }
            false
        })

        cancel.setOnClickListener { onBackPressed() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPassChanged() {
        toast(getString(com.app.belcobtm.R.string.password_changed))
        finish()
    }

    override fun onPinChanged() {
        toast(getString(com.app.belcobtm.R.string.pin_changed))
        finish()
    }
}
