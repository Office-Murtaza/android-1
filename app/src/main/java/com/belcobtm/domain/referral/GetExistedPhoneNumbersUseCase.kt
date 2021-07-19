package com.belcobtm.domain.referral

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.contacts.ContactsRepository

class GetExistedPhoneNumbersUseCase(
    private val contactsRepository: ContactsRepository,
    private val referralRepository: ReferralRepository
) : UseCase<List<String>, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, List<String>> {
        val allContacts = contactsRepository.loadContacts("")
        return referralRepository.getExistingPhones(allContacts.map {
            it.phoneNumber.replace("[-() ]".toRegex(), "")
        })
    }

}