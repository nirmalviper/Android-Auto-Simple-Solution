package net.mfuertes.aagw.gateway.core

enum class GatewayState {
    IDLE,
    CAR_DETECTED,
    INITIALIZING,
    WAITING_FOR_PHONE,
    PHONE_SELECTED,
    BLUETOOTH_CONNECTING,
    BLUETOOTH_CONNECTED,
    WIFI_STARTING,
    WIFI_READY,
    WAITING_FOR_TCP,
    ANDROID_AUTO_CONNECTING,
    CONNECTED,
    DISCONNECTING,
    PHONE_GONE,
    ERROR,
    RECOVERING;

    fun nextForNoPhone(): GatewayState = when (this) {
        IDLE,
        CAR_DETECTED,
        INITIALIZING,
        WAITING_FOR_PHONE,
        PHONE_SELECTED,
        BLUETOOTH_CONNECTING,
        BLUETOOTH_CONNECTED,
        WIFI_STARTING,
        WIFI_READY,
        WAITING_FOR_TCP,
        ANDROID_AUTO_CONNECTING,
        CONNECTED,
        DISCONNECTING,
        PHONE_GONE,
        ERROR,
        RECOVERING -> WAITING_FOR_PHONE
    }

    fun nextForTemporaryLoss(): GatewayState = when (this) {
        CONNECTED,
        ANDROID_AUTO_CONNECTING,
        WAITING_FOR_TCP,
        WIFI_READY,
        BLUETOOTH_CONNECTED,
        PHONE_SELECTED -> WAITING_FOR_PHONE
        else -> WAITING_FOR_PHONE
    }
}
