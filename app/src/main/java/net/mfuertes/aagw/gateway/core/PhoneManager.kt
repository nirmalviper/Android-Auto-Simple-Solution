package net.mfuertes.aagw.gateway.core

class PhoneManager {
    private val knownPhones = linkedSetOf<KnownPhone>()
    private var sessionSelectionId: String? = null

    fun setKnownPhones(phones: List<KnownPhone>) {
        knownPhones.clear()
        knownPhones.addAll(phones)
        if (sessionSelectionId == null) {
            sessionSelectionId = selectPhoneForSession()?.id
        }
    }

    fun getKnownPhones(): List<KnownPhone> = knownPhones.toList()

    fun currentSessionSelectionId(): String? = sessionSelectionId

    fun setSessionSelection(phoneId: String?) {
        sessionSelectionId = phoneId
        val updated = knownPhones.map { known ->
            known.copy(isPreferred = known.id == phoneId || (phoneId == null && known.isPreferred))
        }
        if (updated.isNotEmpty()) {
            knownPhones.clear()
            knownPhones.addAll(updated)
        }
    }

    fun clearSessionSelection() {
        sessionSelectionId = null
    }

    fun setPreferredPhone(phoneId: String?) {
        sessionSelectionId = phoneId
        val updated = knownPhones.map { phone ->
            phone.copy(isPreferred = phone.id == phoneId)
        }
        knownPhones.clear()
        knownPhones.addAll(updated)
    }

    fun upsertKnownPhone(phone: KnownPhone): KnownPhone {
        val existingIndex = knownPhones.indexOfFirst { existing ->
            existing.id == phone.id ||
                (existing.bluetoothAddress != null && existing.bluetoothAddress == phone.bluetoothAddress)
        }

        val merged = if (existingIndex >= 0) {
            val existing = knownPhones.elementAt(existingIndex)
            existing.copy(
                name = phone.name.ifBlank { existing.name },
                bluetoothAddress = phone.bluetoothAddress ?: existing.bluetoothAddress,
                lastConnected = phone.lastConnected ?: existing.lastConnected,
                connectionCount = maxOf(existing.connectionCount, phone.connectionCount),
                isPreferred = existing.isPreferred || phone.isPreferred,
                autoConnect = existing.autoConnect && phone.autoConnect,
                available = existing.available || phone.available
            )
        } else {
            phone
        }

        if (existingIndex >= 0) {
            val updated = knownPhones.toMutableList()
            updated[existingIndex] = merged
            knownPhones.clear()
            knownPhones.addAll(updated)
        } else {
            knownPhones.add(merged)
        }

        if (sessionSelectionId == null && merged.isPreferred) {
            sessionSelectionId = merged.id
        }

        return merged
    }

    fun markPhoneAvailable(phoneId: String?, available: Boolean): KnownPhone? {
        val target = phoneId ?: return null
        val existing = knownPhones.firstOrNull { it.id == target }
            ?: return null
        val updated = existing.copy(available = available)
        val updatedList = knownPhones.toMutableList().apply {
            val index = indexOf(existing)
            if (index >= 0) this[index] = updated
        }
        knownPhones.clear()
        knownPhones.addAll(updatedList)
        return updated
    }

    fun recordPhoneConnection(phoneId: String?, timeMillis: Long = System.currentTimeMillis()): KnownPhone? {
        val target = phoneId ?: return null
        val existing = knownPhones.firstOrNull { it.id == target }
            ?: return null
        val updated = existing.copy(
            lastConnected = timeMillis,
            connectionCount = existing.connectionCount + 1,
            available = true
        )
        val updatedList = knownPhones.toMutableList().apply {
            val index = indexOf(existing)
            if (index >= 0) this[index] = updated
        }
        knownPhones.clear()
        knownPhones.addAll(updatedList)
        return updated
    }

    fun selectPhoneForSession(): KnownPhone? {
        val candidates = knownPhones.toList()
        if (candidates.isEmpty()) {
            return null
        }

        val explicit = sessionSelectionId?.let { id -> candidates.firstOrNull { it.id == id } }
        if (explicit != null) {
            return explicit
        }

        val preferred = candidates.firstOrNull { it.isPreferred }
        if (preferred != null) {
            return preferred
        }

        val mostRecent = candidates.sortedByDescending { it.lastConnected ?: 0L }.firstOrNull()
        if (mostRecent != null) {
            return mostRecent
        }

        return candidates.first()
    }
}
