package net.mfuertes.aagw.gateway

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.mfuertes.aagw.gateway.connectivity.WifiHelper

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("manage_phones")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), PhoneManagementActivity::class.java))
            true
        }

        findPreference<Preference>("start_stop_ap")?.setOnPreferenceClickListener {
            WifiHelper.startP2pAp(requireContext(), null) { hotspot ->
                AlertDialog.Builder(requireContext())
                    .setTitle("HotSpot")
                    .setMessage("Network name: ${hotspot.ssid}\nPassword: ${hotspot.psk}")
                    .setNegativeButton("Stop") { _, _ ->
                        WifiHelper.stopP2pAp(requireContext())
                    }
                    .setOnDismissListener {
                        WifiHelper.stopP2pAp(requireContext())
                    }
                    .show()
            }
            true
        }

        val bluetoothPermission = findPreference<Preference>("bluetooth_permissions")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val group = preferenceScreen?.findPreference<Preference>("permissions")
            group?.isVisible = false
        }

        bluetoothPermission?.isEnabled = !isPermissionAccepted(listOf(Manifest.permission.BLUETOOTH_CONNECT))
        bluetoothPermission?.setOnPreferenceClickListener {
            requestPermission(listOf(Manifest.permission.BLUETOOTH_CONNECT))
            true
        }

        val locationPermission = findPreference<Preference>("location_permissions")
        locationPermission?.isEnabled = !isPermissionAccepted(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        )
        locationPermission?.setOnPreferenceClickListener {
            requestPermission(
                listOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
            true
        }
    }

    private fun requestPermission(permissions: List<String>) {
        val missing = permissions.filter { activity?.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), 0)
        }
    }

    private fun isPermissionAccepted(permissions: List<String>): Boolean {
        return permissions.all { activity?.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }
}
