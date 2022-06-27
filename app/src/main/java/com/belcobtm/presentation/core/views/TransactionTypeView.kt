package com.belcobtm.presentation.core.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.domain.transaction.type.TransactionType
import com.belcobtm.presentation.core.extensions.getResText
import kotlin.math.min

class TransactionTypeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textBounds = Rect()
    private val horizontalPadding = resources.getDimensionPixelOffset(R.dimen.margin_normal)
    private val verticalPadding = resources.getDimensionPixelOffset(R.dimen.margin_half)
    private val cornerRadius = resources.getDimension(R.dimen.corner_radius)
    private var text: String = ""
    private var mainColor: Int = 0

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
    }

    private val textPaint = TextPaint().apply {
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 12f,
            resources.displayMetrics
        )
        isAntiAlias = true
    }

    init {
        if (isInEditMode) {
            setTransactionType(TransactionType.RECEIVE_TRANSFER)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode != MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val calcWidth = textBounds.width() + 2 * horizontalPadding
            val calcHeight = textBounds.height() + 2 * verticalPadding
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                if (heightMode == MeasureSpec.UNSPECIFIED) {
                    setMeasuredDimension(calcWidth, calcHeight)
                } else {
                    val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
                    val boundedHeight = min(calcHeight, maxHeight)
                    setMeasuredDimension(calcWidth, boundedHeight)
                }
            } else {
                if (heightMode == MeasureSpec.UNSPECIFIED) {
                    val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
                    val boundedWidth = min(calcWidth, maxWidth)
                    setMeasuredDimension(boundedWidth, calcHeight)
                } else {
                    val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
                    val boundedWidth = min(calcWidth, maxWidth)
                    val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
                    val boundedHeight = min(calcHeight, maxHeight)
                    setMeasuredDimension(boundedWidth, boundedHeight)
                }
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setTransactionType(type: TransactionType) {
        val textRes = type.getResText()

        val mainColorRes = when (type) {
            TransactionType.UNKNOWN,
            TransactionType.DEPOSIT,
            TransactionType.RECEIVE_TRANSFER,
            TransactionType.ATM_SELL,
            TransactionType.RECEIVE_SWAP,
            TransactionType.RECALL,
            TransactionType.WITHDRAW_STAKE -> R.color.color_status_1
            TransactionType.WITHDRAW,
            TransactionType.SEND_TRANSFER,
            TransactionType.ATM_BUY,
            TransactionType.SEND_SWAP,
            TransactionType.RESERVE,
            TransactionType.CREATE_STAKE -> R.color.color_status_2
            TransactionType.MOVE,
            TransactionType.SELF,
            TransactionType.CANCEL_STAKE -> R.color.color_status_3
            else -> R.color.color_status_1
        }
        mainColor = ContextCompat.getColor(context, mainColorRes)
        text = resources.getString(textRes)
        // re measure the view
        requestLayout()
    }

    override fun onDraw(canvas: Canvas?) {
        backgroundPaint.color = mainColor
        backgroundPaint.alpha = 38 // 15 %
        textPaint.color = mainColor
        val xPos = width / 2f
        val yPos = height / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas?.drawRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, backgroundPaint
        )
        canvas?.drawText(text, xPos, yPos, textPaint)
    }
}
