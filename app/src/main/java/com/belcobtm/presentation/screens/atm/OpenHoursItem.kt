package com.belcobtm.presentation.screens.atm

import android.os.Parcel
import android.os.Parcelable
import com.belcobtm.presentation.core.adapter.model.ListItem

data class OpenHoursItem(
    val day: String,
    val hours: String,
    val isActive: Boolean,
    val isClosed: Boolean
) : ListItem, Parcelable {

    override val id: String
        get() = day

    override val type: Int
        get() = OPEN_HOURS_ITEM_LIST_TYPE

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(day)
        parcel.writeString(hours)
        parcel.writeByte(if (isActive) 1 else 0)
        parcel.writeByte(if (isClosed) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OpenHoursItem> {

        const val OPEN_HOURS_ITEM_LIST_TYPE = 8262

        override fun createFromParcel(parcel: Parcel): OpenHoursItem {
            return OpenHoursItem(parcel)
        }

        override fun newArray(size: Int): Array<OpenHoursItem?> {
            return arrayOfNulls(size)
        }
    }
}