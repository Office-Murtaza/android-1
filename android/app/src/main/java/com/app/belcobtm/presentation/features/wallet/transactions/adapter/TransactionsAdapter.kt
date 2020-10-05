package com.app.belcobtm.presentation.features.wallet.transactions.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.presentation.core.extensions.getResText
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.ui.BaseDiffCallback
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsAdapterItem
import kotlinx.android.synthetic.main.item_transaction.view.*

class TransactionsAdapter(
    private val itemClickListener: (item: TransactionsAdapterItem) -> Unit,
    private val endListListener: () -> Unit
) :
    RecyclerView.Adapter<TransactionsAdapter.Holder>() {
    private val itemList: MutableList<TransactionsAdapterItem> = mutableListOf()

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        val holder =
            Holder(view)
        view.setOnClickListener { itemClickListener.invoke(itemList[holder.adapterPosition]) }
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val context = holder.itemView.context
        val item = itemList[position]

        with(holder.itemView) {
            dateView.text = item.date
            typeView.text = context.getString(item.type.getResText())
            amountView.text = item.cryptoAmount.toStringCoin()
            updateStatusView(statusView, item.status)
        }

        if (position >= itemList.size - 1) {
            endListListener.invoke()
        }
    }

    fun setItemList(itemList: List<TransactionsAdapterItem>) {
        DiffUtil.calculateDiff(BaseDiffCallback(this.itemList, itemList)).dispatchUpdatesTo(this)
        this.itemList.clear()
        this.itemList.addAll(itemList)
    }

    private fun updateStatusView(textView: AppCompatTextView, status: TransactionStatusType) {
        val resText: Int
        val resBackground: Int
        val resTextColor: Int

        when (status) {
            TransactionStatusType.PENDING -> {
                resText = R.string.transaction_status_pending
                resBackground = R.drawable.bg_status_pending
                resTextColor = R.color.colorStatusPending
            }
            TransactionStatusType.COMPLETE -> {
                resTextColor = R.color.colorStatusComplete
                resBackground = R.drawable.bg_status_complete
                resText = R.string.transaction_status_complete
            }
            TransactionStatusType.FAIL -> {
                resText = R.string.transaction_status_fail
                resBackground = R.drawable.bg_status_fail
                resTextColor = R.color.colorStatusFail
            }
            else -> {
                resText = R.string.transaction_status_unknown
                resBackground = R.drawable.bg_status_unknown
                resTextColor = R.color.colorStatusUnknown
            }
        }

        textView.setTextColor(ContextCompat.getColor(textView.context, resTextColor))
        textView.setBackgroundDrawable(ContextCompat.getDrawable(textView.context, resBackground))
        textView.setText(resText)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
