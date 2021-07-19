package com.belcobtm.data

import com.belcobtm.BuildConfig
import com.belcobtm.R
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.referral.ReferralApiService
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.referral.ReferralRepository
import com.belcobtm.domain.referral.item.ReferralDataItem
import com.belcobtm.presentation.core.provider.string.StringProvider

class ReferralRepositoryImpl(
    private val preferencesHelper: SharedPreferencesHelper,
    private val referralApiService: ReferralApiService,
    private val stringProvider: StringProvider
) : ReferralRepository {

    override suspend fun loadStatistic(): Either<Failure, ReferralDataItem> {
        val referralLink = "${BuildConfig.REFERRAL_URL}${preferencesHelper.referralCode}"
        return Either.Right(
            ReferralDataItem(
                referralLink,
                stringProvider.getString(R.string.referral_message_format, referralLink),
                preferencesHelper.referralInvites,
                preferencesHelper.referralEarned
            )
        )
    }

    override suspend fun getExistingPhones(allContacts: List<String>): Either<Failure, List<String>> =
        referralApiService.getExistedPhones(allContacts)
}