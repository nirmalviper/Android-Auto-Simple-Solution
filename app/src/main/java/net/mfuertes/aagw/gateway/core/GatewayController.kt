package net.mfuertes.aagw.gateway.core

class GatewayController(
    private val phoneManager: PhoneManager = PhoneManager()
) {
    private var state: GatewayState = GatewayState.IDLE

    fun currentState(): GatewayState = state

    fun onCarDetected() {
        state = GatewayState.CAR_DETECTED
    }

    fun startInitializing() {
        state = GatewayState.INITIALIZING
    }

    fun onPhoneSelected(phone: KnownPhone?) {
        if (phone != null) {
            phoneManager.setSessionSelection(phone.id)
        }
        state = if (phone != null) GatewayState.PHONE_SELECTED else GatewayState.WAITING_FOR_PHONE
    }

    fun onPhoneAvailable() {
        val selected = phoneManager.selectPhoneForSession()
        if (selected != null) {
            phoneManager.setSessionSelection(selected.id)
            state = GatewayState.PHONE_SELECTED
        } else {
            state = GatewayState.WAITING_FOR_PHONE
        }
    }

    fun onRecoveryNeeded() {
        state = GatewayState.RECOVERING
    }

    fun onPhoneGone() {
        state = GatewayState.PHONE_GONE
    }

    fun onConnected() {
        state = GatewayState.CONNECTED
    }

    fun onError() {
        state = GatewayState.ERROR
    }

    fun onSessionRecovered() {
        state = GatewayState.RECOVERING
    }

    fun resetToWaiting() {
        state = GatewayState.WAITING_FOR_PHONE
    }
}
