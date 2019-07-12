package com.app.belcobtm.api.model

import com.google.gson.annotations.SerializedName

/**
 * Created by ADMIN on 17.07.2018.
 */
class ServerException(@SerializedName("errorCode") var code: Int?, @SerializedName("errorMessage") var errorMessage: String?) :
    Throwable(errorMessage)