package com.app.belcobtm.ui.main.coins.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.TransactionModel
import kotlinx.android.synthetic.main.item_transaction.view.*


class TransactionsAdapter(
    private val mTransactionList: ArrayList<TransactionModel>,
    private val onLoadNextCallback: () -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < mTransactionList.size) {
            val context = holder.itemView.context
            val item = mTransactionList[position]

            var transactionStatusBgId: Int
            var transactionStatusTextId: Int

//            STATUS
//            unknown(0),
//            pending(1),
//            complete(2),
//            fail(3)
            when (item.status) {
                1 -> {
                    transactionStatusBgId = R.drawable.bg_transaction_status_pending
                    transactionStatusTextId = R.string.pending
                }
                2 -> {
                    transactionStatusBgId = R.drawable.bg_transaction_status_complete
                    transactionStatusTextId = R.string.complete
                }
                3 -> {
                    transactionStatusBgId = R.drawable.bg_transaction_status_fail
                    transactionStatusTextId = R.string.fail
                }
                else -> {
                    transactionStatusBgId = R.drawable.bg_transaction_status_unkown
                    transactionStatusTextId = R.string.unknown
                }
            }

//            TYPE
//            deposit(1),
//            withdraw(2),
//            send gift(3),
//            receive gift(4),
//            buy(5),
//            sell(6)
            var transactionTypeTextId = when (item.type) {
                1 -> R.string.deposit
                2 -> R.string.withdraw
                3 -> R.string.send_gift
                4 -> R.string.receive_gift
                5 -> R.string.buy
                else -> R.string.sell
            }

            holder.itemView.transaction_date.text = item.date
            holder.itemView.transaction_amount.text = item.value.toString()
            holder.itemView.transaction_status.text = context.getString(transactionStatusTextId)
            holder.itemView.transaction_status.background = context.getDrawable(transactionStatusBgId)
            holder.itemView.transaction_type.text = context.getString(transactionTypeTextId)
            holder.itemView.transaction_amount.text = item.value.toString()

            if (position >= mTransactionList.size - 1) {
                onLoadNextCallback()
            }
        }
    }

    override fun getItemCount(): Int {
        return mTransactionList.size
    }
}
