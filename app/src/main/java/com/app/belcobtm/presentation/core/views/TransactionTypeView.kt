package com.app.belcobtm.presentation.core.views

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
import com.app.belcobtm.R
import com.app.belcobtm.domain.transaction.type.TransactionType
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
        val textRes = when (type) {
            TransactionType.UNKNOWN -> R.string.transaction_type_unknown
            TransactionType.DEPOSIT -> R.string.transaction_type_deposit
            TransactionType.WITHDRAW -> R.string.transaction_type_withdraw
            TransactionType.SEND_TRANSFER -> R.string.transaction_type_send_transfer
            TransactionType.RECEIVE_TRANSFER -> R.string.transaction_type_receive_transfer
            TransactionType.BUY -> R.string.transaction_type_buy
            TransactionType.SELL -> R.string.transaction_type_sell
            TransactionType.MOVE -> R.string.transaction_type_move
            TransactionType.SWAP_SEND -> R.string.transaction_type_send_exchange
            TransactionType.SWAP_RECEIVE -> R.string.transaction_type_receive_exchange
            TransactionType.RESERVE -> R.string.transaction_type_reserve
            TransactionType.RECALL -> R.string.transaction_type_recall
            TransactionType.SELF -> R.string.transaction_type_self
            TransactionType.STAKE_CREATE -> R.string.transaction_type_stake_create
            TransactionType.STAKE_CANCEL -> R.string.transaction_type_stake_cancel
            TransactionType.STAKE_WITHDRAW -> R.string.transaction_type_stake_withdraw
        }
        val mainColorRes = when (type) {
            TransactionType.UNKNOWN,
            TransactionType.DEPOSIT,
            TransactionType.RECEIVE_TRANSFER,
            TransactionType.BUY,
            TransactionType.SWAP_RECEIVE,
            TransactionType.RECALL,
            TransactionType.STAKE_WITHDRAW -> R.color.color_status_1
            TransactionType.WITHDRAW,
            TransactionType.SEND_TRANSFER,
            TransactionType.SELL,
            TransactionType.SWAP_SEND,
            TransactionType.RESERVE,
            TransactionType.STAKE_CREATE -> R.color.color_status_2
            TransactionType.MOVE,
            TransactionType.SELF,
            TransactionType.STAKE_CANCEL -> R.color.color_status_3
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
