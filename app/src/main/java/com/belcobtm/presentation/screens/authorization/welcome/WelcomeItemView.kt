package com.belcobtm.presentation.screens.authorization.welcome

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.belcobtm.R
import com.belcobtm.databinding.ViewWelcomeItemBinding

class WelcomeItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewWelcomeItemBinding =
        ViewWelcomeItemBinding.inflate(LayoutInflater.from(context), this)

    init {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.WelcomeItemView, 0, 0)
        try {
            setImage(typedArray.getDrawable(R.styleable.WelcomeItemView_welcomeItemSrc))
            setTitle(typedArray.getString(R.styleable.WelcomeItemView_welcomeItemTitle))
            setDescription(typedArray.getString(R.styleable.WelcomeItemView_welcomeItemDescription))
        } finally {
            typedArray.recycle()
        }
    }

    private fun setImage(imageRes: Drawable?) {
        imageRes?.let {
            binding.imageView.setImageDrawable(it)
        }
    }

    private fun setTitle(text: CharSequence?) {
        binding.titleTextView.text = text ?: ""
    }

    private fun setDescription(text: CharSequence?) {
        binding.descriptionTextView.text = text ?: ""
    }

}
