package net.mfuertes.aagw.gateway.core

class PhonePresenceTracker(
    private val phoneManager: PhoneManager,
    private val gracePeriodMs: Long = 15_000L
) {
    private var currentState: GatewayState = GatewayState.WAITING_FOR_PHONE

    fun currentState(): GatewayState = currentState

    fun onPhoneObserved(phone: KnownPhone): GatewayState {
        val savedPhone = phoneManager.upsertKnownPhone(phone)
        phoneManager.setSessionSelection(savedPhone.id)
        phoneManager.markPhoneAvailable(savedPhone.id, true)
        phoneManager.recordPhoneConnection(savedPhone.id)
        currentState = if (savedPhone.id == phoneManager.currentSessionSelectionId()) {
            GatewayState.PHONE_SELECTED
        } else {
            GatewayState.WAITING_FOR_PHONE
        }
        return currentState
    }

    fun onPhoneMissing(phoneId: String? = phoneManager.currentSessionSelectionId()): GatewayState {
        val targetId = phoneId ?: return GatewayState.WAITING_FOR_PHONE.also { currentState = it }
        phoneManager.markPhoneAvailable(targetId, false)
        currentState = GatewayState.RECOVERING
        return currentState
    }

    fun onPhoneRecovered(phoneId: String? = phoneManager.currentSessionSelectionId()): GatewayState {
        val targetId = phoneId ?: return GatewayState.WAITING_FOR_PHONE.also { currentState = it }
        phoneManager.markPhoneAvailable(targetId, true)
        phoneManager.recordPhoneConnection(targetId)
        currentState = GatewayState.PHONE_SELECTED
        return currentState
    }

    fun onConnectionLost(): GatewayState {
        currentState = GatewayState.PHONE_GONE
        return currentState
    }

    fun shouldRecoverNow(): Boolean = currentState == GatewayState.RECOVERING && System.currentTimeMillis() - lastObservedAt() > gracePeriodMs

    private fun lastObservedAt(): Long {
        return phoneManager.getKnownPhones()
            .firstOrNull { it.id == phoneManager.currentSessionSelectionId() }
            ?.lastConnected
            ?: 0L
    }
}
