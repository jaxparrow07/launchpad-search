package com.devrinth.launchpad.search.plugins

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.devrinth.launchpad.adapters.ResultAdapter
import com.devrinth.launchpad.search.SearchPlugin
import com.devrinth.launchpad.utils.IntentUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsPlugin(mContext: Context) : SearchPlugin(mContext) {

    override var ID = "contacts"

    private lateinit var mContentResolver : ContentResolver
    private val nameProjection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.PHOTO_URI,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    )
    private val phoneProjection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    private var isProcessing = false

    override fun pluginInit() {
        INIT = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (INIT) {
            mContentResolver = mContext.contentResolver
        } else {
            Toast.makeText(mContext, "Contacts permission needed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun pluginProcess(query: String) {
        if (!INIT || query.isEmpty() || query.length < 2 || isProcessing) {
            pluginResult(emptyList(), "")
            return
        }
        isProcessing = false

        CoroutineScope(Dispatchers.Main).launch {
            pluginResult(filterContacts(query), query)
            isProcessing = false
        }
    }

    @SuppressLint("Range")
    private suspend fun filterContacts(query: String): List<ResultAdapter> {
        return withContext(Dispatchers.Default) {
            val filteredContacts = arrayListOf<ResultAdapter>()

            var selection: String? = null
            var selectionArgs: Array<String>? = null
            if (query.isNotEmpty()) {
                selection = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
                selectionArgs = arrayOf("%${query.lowercase()}%")
            }

            val cursor = mContentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                nameProjection,
                selection,
                selectionArgs,
                ContactsContract.Contacts.SORT_KEY_PRIMARY
            )

            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        val contactId =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        val displayName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                        val photoUri: String? =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))

                        val contactPhoto: Drawable? = try {
                            getContactPhotoDrawable(Uri.parse(photoUri))
                        } catch (e: Exception) {
                            null
                        }

                        val phoneCursor = mContentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            phoneProjection,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )

                        var phoneNumber: String? = null
                        if (phoneCursor != null && phoneCursor.moveToFirst()) {
                            phoneNumber =
                                phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            phoneCursor.close()
                        }

                        if (displayName.contains(query.trim(), true)) {
                            filteredContacts.add(
                                ResultAdapter(
                                    displayName,
                                    phoneNumber ?: "",
                                    contactPhoto,
                                    IntentUtils.getCallIntent((phoneNumber ?: "").trim()),
                                    null
                                )
                            )
                        }
                    }
                    cursor.close()
                }
            } catch (_: Exception) {}
            filteredContacts
        }
    }

    private fun getContactPhotoDrawable(photoUri: Uri?): Drawable? {
        if (photoUri == null) {
            return null
        }
        val contentResolver = mContext.contentResolver
        return try {
            val inputStream = contentResolver.openInputStream(photoUri)
            Drawable.createFromStream(inputStream, photoUri.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}