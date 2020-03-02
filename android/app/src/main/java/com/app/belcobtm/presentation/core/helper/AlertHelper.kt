package com.app.belcobtm.presentation.core.helper

import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.app.belcobtm.R

object AlertHelper {
    fun showToastShort(context: Context, resText: Int) = showToast(context, resText, Toast.LENGTH_SHORT)

    fun showToastLong(context: Context, resText: Int) = showToast(context, resText, Toast.LENGTH_LONG)

    private fun showToast(context: Context, resText: Int, duration: Int) {
        Toast.makeText(context, resText, duration).also {
            val stateList = ContextCompat.getColorStateList(context, R.color.toast_background)
            DrawableCompat.setTintList(it.view.background, stateList)
            it.view.findViewById<TextView>(android.R.id.message)
                .setTextColor(ContextCompat.getColor(context, R.color.toast_text))
        }.show()
    }

    fun showToastShort(context: Context, text: String?) = showToast(context, text, Toast.LENGTH_SHORT)

    fun showToastLong(context: Context, text: String?) = showToast(context, text, Toast.LENGTH_LONG)

    private fun showToast(context: Context, text: String?, duration: Int) {
        Toast.makeText(context, text, duration).also {
            val stateList = ContextCompat.getColorStateList(context, R.color.toast_background)
            DrawableCompat.setTintList(it.view.background, stateList)
            it.view.findViewById<TextView>(android.R.id.message)
                .setTextColor(ContextCompat.getColor(context, R.color.toast_text))
        }.show()
    }
}