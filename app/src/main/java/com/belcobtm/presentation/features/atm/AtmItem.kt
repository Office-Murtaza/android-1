package com.belcobtm.presentation.features.atm

import android.os.Parcel
import android.os.Parcelable
import com.belcobtm.data.rest.atm.response.OperationType
import com.google.android.gms.maps.model.LatLng
import java.lang.IllegalStateException

data class AtmItem(
    val latLng: LatLng,
    val title: String,
    val address: String,
    val openHours: List<OpenHoursItem>,
    val distance: String,
    @OperationType val type: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(LatLng::class.java.classLoader) ?: throw IllegalStateException(),
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.createTypedArrayList(OpenHoursItem).orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(latLng, flags)
        parcel.writeString(title)
        parcel.writeString(address)
        parcel.writeTypedList(openHours)
        parcel.writeString(distance)
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AtmItem> {
        override fun createFromParcel(parcel: Parcel): AtmItem {
            return AtmItem(parcel)
        }

        override fun newArray(size: Int): Array<AtmItem?> {
            return arrayOfNulls(size)
        }
    }
}