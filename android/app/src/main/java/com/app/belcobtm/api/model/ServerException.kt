package com.app.belcobtm.api.model

import com.google.gson.annotations.SerializedName


class ServerException(@SerializedName("errorCode") var code: Int?, @SerializedName("errorMessage") var errorMessage: String?) :
    Throwable(errorMessage)