package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TronBlockResponse(
    @SerializedName("blockHeader")
    val blockHeader: BlockHeaderEntity?
) : Serializable

data class BlockHeaderEntity(
    @SerializedName("raw_data")
    val raw_data: RawDataEntity?
) : Serializable


data class RawDataEntity(
    @SerializedName("number")
    val number: Long?,
    @SerializedName("txTrieRoot")
    val txTrieRoot: String?,
    @SerializedName("witness_address")
    val witnessAddress: String?,
    @SerializedName("parentHash")
    val parentHash: String?,
    @SerializedName("version")
    val version: Int?,
    @SerializedName("timestamp")
    val timestamp: Long?

) : Serializable