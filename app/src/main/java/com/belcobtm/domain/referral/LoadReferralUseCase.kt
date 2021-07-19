package com.belcobtm.domain.referral

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.referral.item.ReferralDataItem

class LoadReferralUseCase(
    private val repository: ReferralRepository
) : UseCase<ReferralDataItem, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, ReferralDataItem> =
        repository.loadStatistic()
}