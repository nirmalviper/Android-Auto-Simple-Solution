package net.mfuertes.aagw.gateway

import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import net.mfuertes.aagw.gateway.connectivity.WifiHelper

@SuppressLint("ExportedPreferenceActivity")
class USBReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()

        WifiHelper.stopP2pAp(this)

        intent.getStringExtra("MAC_ADDRESS")?.let {
            PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
                putString(GatewayService.MAC_ADDRESS_KEY, it).apply()
            }
            finish()
        }

        if (intent.action?.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED) == true) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            val accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY) as UsbAccessory?
            accessory?.also { usbAccessory ->
                val i = Intent(this, GatewayService::class.java)
                i.putExtra(UsbManager.EXTRA_ACCESSORY, usbAccessory)
                i.putExtra(
                    GatewayService.MAC_ADDRESS_KEY,
                    sharedPref.getString(GatewayService.MAC_ADDRESS_KEY, null)
                )
                startForegroundService(i)
            }
            finish()
        }
    }
}
