package com.belcobtm.presentation.tools.extensions

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.showError(resText: Int?) = if (resText == null) {
    error = null
} else {
    error = resources.getString(resText)
}

fun TextInputLayout.clearError() {
    error = null
}

fun TextInputLayout.getString(): String = editText?.text?.toString() ?: ""

fun TextInputLayout.getDouble(): Double = when {
    (editText?.text?.isEmpty() == true)
            || (editText?.text?.toString()?.replace("[^\\d]", "")?.length == 0) -> 0.0
    else -> {
        editText?.text?.toString()?.toDouble() ?: 0.0
    }
}

fun TextInputLayout.setText(text: String) = editText?.setText(text)

fun TextInputLayout.clearText() = editText?.setText("")

fun TextInputLayout.isNotBlank() = editText?.getString()?.isNotBlank() ?: false

fun TextInputLayout.actionDoneListener(listener: () -> Unit) {
    editText?.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            listener.invoke()
        }
        false
    }
}

fun TextInputLayout.setResizedDrawableStart(resDrawable: Int, resDrawableEnd: Int = 0) {
    val imageSize: Int = (24 * resources.displayMetrics.density).toInt()
    val drawableStart: Drawable? = BitmapDrawable(
        resources,
        Bitmap.createScaledBitmap(
            ContextCompat.getDrawable(context, resDrawable)!!.toBitmap(),
            imageSize,
            imageSize,
            false
        )
    )
    val drawableEnd: Drawable? = ContextCompat.getDrawable(context, resDrawableEnd)
    editText?.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, null, drawableEnd, null)
}

fun TextInputLayout.setDrawableStartEnd(resDrawableStart: Int, resDrawableEnd: Int) =
    editText?.setCompoundDrawablesRelativeWithIntrinsicBounds(resDrawableStart, 0, resDrawableEnd, 0)