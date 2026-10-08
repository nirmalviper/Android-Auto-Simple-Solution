package net.mfuertes.aagw.gateway

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import net.mfuertes.aagw.gateway.core.GatewayPreferences
import net.mfuertes.aagw.gateway.core.GatewayState
import net.mfuertes.aagw.gateway.core.KnownPhone
import net.mfuertes.aagw.gateway.core.PhoneManager

class PhoneManagementFragment : PreferenceFragmentCompat() {
    private val gatewayPreferences by lazy { GatewayPreferences(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
        refreshPhoneList()
    }

    override fun onResume() {
        super.onResume()
        refreshPhoneList()
    }

    private fun refreshPhoneList() {
        val screen = preferenceScreen ?: return
        screen.removeAll()

        val statusCategory = PreferenceCategory(requireContext()).apply { title = "Gateway status" }
        screen.addPreference(statusCategory)

        statusCategory.addPreference(
            Preference(requireContext()).apply {
                key = "gateway_state"
                title = "Current state"
                summary = GatewayState.WAITING_FOR_PHONE.name
            }
        )

        val selectedPhoneId = gatewayPreferences.getPreferredPhoneId()
        statusCategory.addPreference(
            Preference(requireContext()).apply {
                key = "selected_phone"
                title = "Selected phone"
                summary = selectedPhoneId ?: "No preferred phone set"
            }
        )

        val knownPhones = gatewayPreferences.loadKnownPhones()
        val phonesCategory = PreferenceCategory(requireContext()).apply { title = "Known phones" }
        screen.addPreference(phonesCategory)

        if (knownPhones.isEmpty()) {
            phonesCategory.addPreference(
                Preference(requireContext()).apply {
                    key = "no_phones"
                    title = "No phones discovered yet"
                    summary = "Once a phone is paired and seen by the bridge, it will appear here."
                    isSelectable = false
                }
            )
            return
        }

        val phoneManager = PhoneManager().apply { setKnownPhones(knownPhones) }
        knownPhones.sortedWith(
            compareByDescending<KnownPhone> { it.available }
                .thenByDescending { it.lastConnected ?: 0L }
                .thenBy { it.name }
        ).forEach { phone ->
            phonesCategory.addPreference(
                Preference(requireContext()).apply {
                    key = "phone_${phone.id}"
                    title = phone.name
                    summary = buildString {
                        append(if (phone.available) "Available" else "Not currently visible")
                        if (phone.id == selectedPhoneId) {
                            append(" • Preferred")
                        }
                        if (phone.bluetoothAddress != null) {
                            append(" • ${phone.bluetoothAddress}")
                        }
                    }
                    setOnPreferenceClickListener {
                        gatewayPreferences.setPreferredPhoneId(phone.id)
                        phoneManager.setPreferredPhone(phone.id)
                        gatewayPreferences.saveKnownPhones(phoneManager.getKnownPhones())
                        refreshPhoneList()
                        true
                    }
                }
            )
        }
    }
}
