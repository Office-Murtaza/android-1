package com.belcobtm.domain.settings.item

import android.os.Parcel
import android.os.Parcelable
import com.belcobtm.domain.settings.type.VerificationStatus

data class VerificationInfoDataItem(
    val id: String?,
    val status: VerificationStatus,
    val txLimit: Double,
    val dayLimit: Double,
    val message: String,
    val idCardNumberFilename: String,
    val idCardNumber: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val zipCode: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        VerificationStatus.fromString(parcel.readString()),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(status.stringValue)
        parcel.writeDouble(txLimit)
        parcel.writeDouble(dayLimit)
        parcel.writeString(message)
        parcel.writeString(idCardNumberFilename)
        parcel.writeString(idCardNumber)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(address)
        parcel.writeString(city)
        parcel.writeString(country)
        parcel.writeString(province)
        parcel.writeString(zipCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VerificationInfoDataItem> {
        override fun createFromParcel(parcel: Parcel): VerificationInfoDataItem {
            return VerificationInfoDataItem(parcel)
        }

        override fun newArray(size: Int): Array<VerificationInfoDataItem?> {
            return arrayOfNulls(size)
        }
    }

}