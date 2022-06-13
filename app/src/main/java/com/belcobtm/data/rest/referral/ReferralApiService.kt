package com.belcobtm.data.rest.referral

import com.belcobtm.data.rest.referral.request.GetExistedPhonesRequest
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure

class ReferralApiService(
    private val api: ReferralApi
) {

    suspend fun getExistedPhones(allContacts: List<String>): Either<Failure, List<String>> = try {
        val request = api.getExistedPhonesAsync(GetExistedPhonesRequest(allContacts))
        request.body()?.let { response ->
            Either.Right(response.existingPhones)
        } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }

}
