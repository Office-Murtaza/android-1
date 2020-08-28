package com.app.belcobtm.presentation.features.wallet.transactions.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val transactionStatusBgId: Int
        val transactionStatusTextId: Int
        val transactionStatusTextColor: Int
        when (item.status) {
            TransactionStatusType.PENDING -> {
                transactionStatusBgId = R.drawable.bg_status_pending
                transactionStatusTextId = R.string.transaction_status_pending
                transactionStatusTextColor = R.color.colorStatusPending
            }
            TransactionStatusType.COMPLETE -> {
                transactionStatusBgId = R.drawable.bg_status_complete
                transactionStatusTextColor = R.color.colorStatusComplete
                transactionStatusTextId = R.string.transaction_status_complete
            }
            TransactionStatusType.FAIL -> {
                transactionStatusBgId = R.drawable.bg_status_fail
                transactionStatusTextColor = R.color.colorStatusFail
                transactionStatusTextId = R.string.transaction_status_fail
            }
            else -> {
                transactionStatusBgId = R.drawable.bg_status_unkown
                transactionStatusTextColor = R.color.colorStatusUnknown
                transactionStatusTextId = R.string.transaction_status_unknown
            }
        }
        holder.itemView.transaction_date.text = item.date
        holder.itemView.transaction_status.text = context.getString(transactionStatusTextId)
        holder.itemView.transaction_status.setTextColor(ContextCompat.getColor(context, transactionStatusTextColor))
        holder.itemView.transaction_status.background = context.getDrawable(transactionStatusBgId)
        holder.itemView.transaction_type.text = context.getString(item.type.getResText())
        holder.itemView.amountView.text = item.cryptoAmount.toStringCoin()

        if (position >= itemList.size - 1) {
            endListListener.invoke()
        }
    }

    fun setItemList(itemList: List<TransactionsAdapterItem>) {
        DiffUtil.calculateDiff(BaseDiffCallback(this.itemList, itemList)).dispatchUpdatesTo(this)
        this.itemList.clear()
        this.itemList.addAll(itemList)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
