package com.belcobtm.domain.referral

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.referral.item.ReferralDataItem

interface ReferralRepository {

    suspend fun loadStatistic(): Either<Failure, ReferralDataItem>

    suspend fun getExistingPhones(allContacts: List<String>): Either<Failure, List<String>>
}