package net.mfuertes.aagw.gateway

import net.mfuertes.aagw.gateway.core.GatewayController
import net.mfuertes.aagw.gateway.core.GatewayState
import net.mfuertes.aagw.gateway.core.KnownPhone
import net.mfuertes.aagw.gateway.core.PhoneManager
import net.mfuertes.aagw.gateway.core.PhonePresenceTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneManagerTest {
    @Test
    fun `preferred phone wins when multiple known phones are available`() {
        val phoneManager = PhoneManager()

        val john = KnownPhone(
            id = "john",
            name = "John's Pixel 9",
            bluetoothAddress = "AA:BB:CC:DD:EE:FF",
            isPreferred = true,
            autoConnect = true,
            available = true,
            lastConnected = 1000L
        )
        val sarah = KnownPhone(
            id = "sarah",
            name = "Sarah's Galaxy S25",
            bluetoothAddress = "11:22:33:44:55:66",
            isPreferred = false,
            autoConnect = true,
            available = true,
            lastConnected = 900L
        )

        phoneManager.setKnownPhones(listOf(john, sarah))
        val next = phoneManager.selectPhoneForSession()

        assertEquals("john", next?.id)
    }

    @Test
    fun `manual selection overrides preferred choice`() {
        val phoneManager = PhoneManager()

        val john = KnownPhone(
            id = "john",
            name = "John's Pixel 9",
            bluetoothAddress = "AA:BB:CC:DD:EE:FF",
            isPreferred = true,
            autoConnect = true,
            available = true,
            lastConnected = 1000L
        )
        val sarah = KnownPhone(
            id = "sarah",
            name = "Sarah's Galaxy S25",
            bluetoothAddress = "11:22:33:44:55:66",
            isPreferred = false,
            autoConnect = true,
            available = true,
            lastConnected = 900L
        )

        phoneManager.setKnownPhones(listOf(john, sarah))
        phoneManager.setSessionSelection("sarah")

        val next = phoneManager.selectPhoneForSession()
        assertEquals("sarah", next?.id)
    }

    @Test
    fun `gateway state transitions to waiting when no phone is available`() {
        val state = GatewayState.IDLE
        val next = state.nextForNoPhone()

        assertEquals(GatewayState.WAITING_FOR_PHONE, next)
    }

    @Test
    fun `gateway controller reflects connected phone loss and recovery`() {
        val phoneManager = PhoneManager()
        val phone = KnownPhone(
            id = "beta",
            name = "Galaxy",
            bluetoothAddress = "11:22:33:44:55:66",
            isPreferred = true,
            autoConnect = true,
            available = true
        )
        phoneManager.setKnownPhones(listOf(phone))

        val controller = GatewayController(phoneManager)
        controller.onCarDetected()
        controller.startInitializing()
        controller.onPhoneSelected(phone)
        assertEquals(GatewayState.PHONE_SELECTED, controller.currentState())

        controller.onPhoneGone()
        assertEquals(GatewayState.PHONE_GONE, controller.currentState())

        controller.onRecoveryNeeded()
        assertEquals(GatewayState.RECOVERING, controller.currentState())
    }

    @Test
    fun `connected gateway can recover after a temporary phone absence`() {
        val state = GatewayState.CONNECTED
        val next = state.nextForTemporaryLoss()

        assertEquals(GatewayState.WAITING_FOR_PHONE, next)
        assertTrue(next != GatewayState.IDLE)
    }

    @Test
    fun `observed phones are merged and marked as available`() {
        val phoneManager = PhoneManager()
        val tracker = PhonePresenceTracker(phoneManager)

        tracker.onPhoneObserved(
            KnownPhone(
                id = "alpha",
                name = "Pixel",
                bluetoothAddress = "AA:BB:CC:DD:EE:FF",
                isPreferred = true,
                autoConnect = true
            )
        )

        val stored = phoneManager.getKnownPhones().single { it.id == "alpha" }
        assertTrue(stored.available)
        assertEquals(1, stored.connectionCount)
    }

    @Test
    fun `missing phone triggers recovery state`() {
        val phoneManager = PhoneManager()
        val tracker = PhonePresenceTracker(phoneManager)

        val phone = KnownPhone(
            id = "beta",
            name = "Galaxy",
            bluetoothAddress = "11:22:33:44:55:66",
            isPreferred = true,
            autoConnect = true,
            available = true
        )

        phoneManager.setKnownPhones(listOf(phone))
        phoneManager.setSessionSelection("beta")

        tracker.onPhoneMissing("beta")

        assertEquals(GatewayState.RECOVERING, tracker.currentState())
    }
}
