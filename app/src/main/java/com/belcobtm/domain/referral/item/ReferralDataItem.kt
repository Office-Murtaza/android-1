package com.belcobtm.domain.referral.item

data class ReferralDataItem(
    val link: String,
    val message: String,
    val invited: Int,
    val earned: Double
)