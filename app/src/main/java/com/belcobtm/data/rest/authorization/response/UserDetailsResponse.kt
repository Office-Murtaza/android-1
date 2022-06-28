package com.belcobtm.data.rest.authorization.response

data class UserDetailsResponse(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val roles: List<String>?,
    val referralCode: String?,
    val referrals: Int?,
    val referralEarned: Double?,
    val status: String
)
