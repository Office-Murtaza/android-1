package com.belcobtm.data

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.belcobtm.domain.contacts.ContactsRepository
import com.belcobtm.domain.contacts.item.Contact

class ContactsRepositoryImpl(private val contentResolver: ContentResolver) : ContactsRepository {

    override suspend fun loadContacts(searchQuery: String): List<Contact> =
        contentResolver.query(
            Phone.CONTENT_URI,
            arrayOf(
                Phone._ID, Phone.DISPLAY_NAME_PRIMARY, Phone.PHOTO_THUMBNAIL_URI, Phone.NUMBER
            ),
            createSelection(searchQuery), createSelectionArgs(searchQuery),
            "${Phone.DISPLAY_NAME_PRIMARY} ASC"
        )?.use { cursor ->
            val contacts = ArrayList<Contact>()
            while (cursor.moveToNext()) {
                val displayName = cursor.getString(Phone.DISPLAY_NAME_PRIMARY)
                val phoneNumber = cursor.getString(Phone.NUMBER)
                contacts.add(
                    Contact(
                        cursor.getString(Phone._ID),
                        displayName,
                        cursor.getString(Phone.PHOTO_THUMBNAIL_URI),
                        phoneNumber,
                        calculateRangeFor(searchQuery, displayName),
                        calculateRangeFor(searchQuery, phoneNumber)
                    )
                )
            }
            contacts
        }.orEmpty()

    private fun calculateRangeFor(searchQuery: String, sourceString: String): IntRange? =
        sourceString.indexOf(searchQuery).let { indexOfSearchQuery ->
            if (indexOfSearchQuery != -1) {
                indexOfSearchQuery..indexOfSearchQuery + searchQuery.length
            } else {
                null
            }
        }

    private fun createSelection(searchQuery: String): String? =
        "${Phone.DISPLAY_NAME_PRIMARY} LIKE ? OR ${Phone.NUMBER} LIKE ?".takeIf { searchQuery.isNotEmpty() }

    private fun createSelectionArgs(searchQuery: String): Array<String>? =
        arrayOf("%$searchQuery%", "%${searchQuery}%").takeIf { searchQuery.isNotEmpty() }

    private fun Cursor.getString(columnName: String) =
        getString(getColumnIndex(columnName)).orEmpty()

}