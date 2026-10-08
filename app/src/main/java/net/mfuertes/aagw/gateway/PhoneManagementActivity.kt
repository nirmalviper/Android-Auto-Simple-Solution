package net.mfuertes.aagw.gateway

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PhoneManagementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PhoneManagementFragment())
                .commit()
        }
    }
}
