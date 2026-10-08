package net.mfuertes.aagw.gateway.core

import android.content.Context
import android.content.SharedPreferences

class GatewayPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setPreferredPhoneId(phoneId: String?) {
        prefs.edit().putString(KEY_PREFERRED_PHONE_ID, phoneId).apply()
    }

    fun getPreferredPhoneId(): String? = prefs.getString(KEY_PREFERRED_PHONE_ID, null)

    fun saveKnownPhones(phones: List<KnownPhone>) {
        val data = phones.map { phone ->
            listOf(
                phone.id,
                phone.name,
                phone.bluetoothAddress ?: "",
                phone.lastConnected?.toString() ?: "",
                phone.connectionCount.toString(),
                phone.isPreferred.toString(),
                phone.autoConnect.toString(),
                phone.available.toString()
            ).joinToString("|")
        }.joinToString(";")

        prefs.edit().putString(KEY_KNOWN_PHONES, data).apply()
    }

    fun loadKnownPhones(): List<KnownPhone> {
        val value = prefs.getString(KEY_KNOWN_PHONES, "") ?: ""
        if (value.isBlank()) return emptyList()

        return value.split(";").filter { it.isNotBlank() }.map { entry ->
            val fields = entry.split("|")
            val id = fields.getOrNull(0) ?: ""
            val name = fields.getOrNull(1) ?: "Unknown phone"
            val bluetoothAddress = fields.getOrNull(2)?.takeIf { it.isNotBlank() }
            val lastConnected = fields.getOrNull(3)?.takeIf { it.isNotBlank() }?.toLongOrNull()
            val connectionCount = fields.getOrNull(4)?.toIntOrNull() ?: 0
            val isPreferred = fields.getOrNull(5)?.toBooleanStrictOrNull() ?: false
            val autoConnect = fields.getOrNull(6)?.toBooleanStrictOrNull() ?: true
            val available = fields.getOrNull(7)?.toBooleanStrictOrNull() ?: false

            KnownPhone(
                id = id,
                name = name,
                bluetoothAddress = bluetoothAddress,
                lastConnected = lastConnected,
                connectionCount = connectionCount,
                isPreferred = isPreferred,
                autoConnect = autoConnect,
                available = available
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "gateway_phone_manager"
        private const val KEY_PREFERRED_PHONE_ID = "preferred_phone_id"
        private const val KEY_KNOWN_PHONES = "known_phones"
    }
}
