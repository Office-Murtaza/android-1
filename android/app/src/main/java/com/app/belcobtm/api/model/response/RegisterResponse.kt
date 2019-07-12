package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class RegisterResponse(
    @SerializedName("accessToken")
    val accessToken: String, // eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIkMmEkMTAkR1VaR1JKYmhlQ1F0VXlyelV2VjhKT1RjUUxQYWZ4ZWRkbzYwYzBxR3g0cUx2YVVYZnIwQ3kiLCJhdXRoIjoiUk9MRV9VU0VSIiwiZXhwIjoxNTYxODM4NTQzfQ.gEUiZxUsLl_jBR0JckZu9c9KZYd3e0TwOs-LSyL-We3WG4af15jgUvSGOKgRtCW5sOedk_cDupwuAA2yGXA1qw
    @SerializedName("expires")
    val expires: Long, // 1561752443935
    @SerializedName("refreshToken")
    val refreshToken: String, // eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIkMmEkMTAkR1VaR1JKYmhlQ1F0VXlyelV2VjhKT1RjUUxQYWZ4ZWRkbzYwYzBxR3g0cUx2YVVYZnIwQ3kiLCJhdXRoIjoiUk9MRV9VU0VSIiwiZXhwIjoxNTY0MzQ0MTQzfQ.OLfWKPbF8iWdYEv6mrSnlb-Tni3rYXKX7svRIqm-uzQq06iXvCiIb1ujE7Z9RkDA43v8iNmk2q5S_WJkt9O_dA
    @SerializedName("roles")
    val roles: List<String>,
    @SerializedName("userId")
    val userId: Int // 1000003
)