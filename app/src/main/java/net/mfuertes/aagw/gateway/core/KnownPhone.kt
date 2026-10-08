package net.mfuertes.aagw.gateway.core

import java.util.UUID

data class KnownPhone(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val bluetoothAddress: String? = null,
    val lastConnected: Long? = null,
    val connectionCount: Int = 0,
    val isPreferred: Boolean = false,
    val autoConnect: Boolean = true,
    val available: Boolean = false
)
