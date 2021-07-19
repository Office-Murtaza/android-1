package com.belcobtm.domain.referral

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.referral.item.SelectableContact
import com.belcobtm.presentation.core.adapter.model.ListItem

class CreateRecipientsUseCase : UseCase<String, List<ListItem>>() {

    override suspend fun run(params: List<ListItem>): Either<Failure, String> {
        val selectedContacts = params.asSequence()
            .filterIsInstance<SelectableContact>()
            .filter(SelectableContact::isSelected)
            .map { it.contact.phoneNumber }
            .toList()
        if (selectedContacts.isEmpty()) {
            return Either.Right("")
        }
        return Either.Right("smsto:${selectedContacts.joinToString(";")}")
    }
}