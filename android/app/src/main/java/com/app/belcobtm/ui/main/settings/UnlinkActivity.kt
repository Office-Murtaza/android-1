package com.app.belcobtm.ui.main.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.app.belcobtm.R
import com.app.belcobtm.ui.main.settings.check_pass.CheckPassActivity
import kotlinx.android.synthetic.main.activity_unlink.*

class UnlinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlink)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        unlink.setOnClickListener {
            CheckPassActivity.start(this, CheckPassActivity.Companion.Mode.MODE_UNLINK)
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
}
