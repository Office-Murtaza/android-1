import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemBankAccountBinding
import com.belcobtm.domain.bank_account.item.BankAccountListItem
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.features.bank_accounts.delegate.BankAccountItemViewHolder

class BankAccountItemDelegate(
    private val onBankAccountClicked: (BankAccountListItem) -> Unit
) : AdapterDelegate<BankAccountListItem, BankAccountItemViewHolder>() {

    override val viewType: Int
        get() = BankAccountListItem.BANK_ACCOUNT_ITEM_TYPE

    override fun createHolder(
        parent: ViewGroup,
        inflater: LayoutInflater
    ): BankAccountItemViewHolder =
        BankAccountItemViewHolder(
            ItemBankAccountBinding.inflate(inflater, parent, false),
            onBankAccountClicked
        )
}