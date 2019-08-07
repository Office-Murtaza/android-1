package com.app.belcobtm.ui.main.settings.change_pass

import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.main.coins.settings.change_pass.ChangePassContract
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_change_pass.*
import kotlinx.android.synthetic.main.activity_check_pass.*
import kotlinx.android.synthetic.main.activity_check_pass.cancel
import kotlinx.android.synthetic.main.activity_unlink.toolbar
import org.jetbrains.anko.toast

class ChangePassActivity : BaseMvpActivity<ChangePassContract.View, ChangePassContract.Presenter>(),
    ChangePassContract.View {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pass)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        change_pass.setOnClickListener {
            hideSoftKeyboard()
            mPresenter.changePass(
                old_pass.text.toString(),
                new_pass.text.toString(),
                confirm_new_pass.text.toString()
            )
        }

        confirm_new_pass.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                hideSoftKeyboard()
                change_pass.performClick()
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
        //todo
        toast("in progress")
    }
}
