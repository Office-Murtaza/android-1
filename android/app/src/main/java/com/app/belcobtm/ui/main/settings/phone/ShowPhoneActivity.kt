package com.app.belcobtm.ui.main.settings.phone

import android.os.Bundle
import android.view.MenuItem
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.main.coins.settings.phone.ShowPhoneContract
import com.app.belcobtm.ui.main.settings.check_pass.CheckPassActivity
import kotlinx.android.synthetic.main.activity_show_phone.*

class ShowPhoneActivity : BaseMvpActivity<ShowPhoneContract.View, ShowPhoneContract.Presenter>(),
    ShowPhoneContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_phone)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        change_phone.setOnClickListener {
            CheckPassActivity.start(this, CheckPassActivity.Companion.Mode.MODE_OPEN_PHONE)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPhoneReceived(phone: String?) {
        phone_view.text = phone
        //todo add phone formatting
    }
}
