package com.app.belcobtm.ui.main.settings.check_pass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.auth.seed.SeedPhraseActivity
import com.app.belcobtm.ui.main.coins.settings.check_pass.CheckPassContract
import kotlinx.android.synthetic.main.activity_check_pass.*
import kotlinx.android.synthetic.main.activity_unlink.toolbar

class CheckPassActivity : BaseMvpActivity<CheckPassContract.View, CheckPassContract.Presenter>(),
    CheckPassContract.View {

    companion object {
        private const val KEY_MODE = "KEY_MODE"

        fun startActivity(context: Context?, mode: Mode) {
            val intent = Intent(context, CheckPassActivity::class.java)
            intent.putExtra(KEY_MODE, mode.ordinal)
            context?.startActivity(intent)
        }

        enum class Mode {
            MODE_SEED,
            MODE_PHONE,
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
            Companion.Mode.MODE_PHONE -> {
                supportActionBar?.title = getString(R.string.change_phone)
                next.text = getString(R.string.change_phone)
            }
            Companion.Mode.MODE_UNLINK -> {
                supportActionBar?.title = getString(R.string.settings_unlink)
                next.text = getString(R.string.settings_unlink)
            }
            Companion.Mode.MODE_SEED -> {
                supportActionBar?.title = getString(R.string.open_seed)
                next.text = getString(R.string.open_seed)
            }
        }
        next.setOnClickListener {
            mPresenter.checkPass(pass.text.toString())
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
            Companion.Mode.MODE_PHONE -> {

            }
            Companion.Mode.MODE_UNLINK -> {

            }
            Companion.Mode.MODE_SEED -> {
                mPresenter.requestSeed()
                finish()
            }
        }
    }

    override fun onSeedGot(seed: String?) {
        SeedPhraseActivity.startActivity(this, seed, true)
    }
}
