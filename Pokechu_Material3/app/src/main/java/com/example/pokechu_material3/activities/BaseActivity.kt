package com.example.pokechu_material3.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import com.akexorcist.localizationactivity.ui.LocalizationActivity

open class BaseActivity: LocalizationActivity() { //You can use your preferred activity instead of AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resetTitle()
    }

    override fun onAfterLocaleChanged() {
        super.onAfterLocaleChanged()

        resetTitle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.getItemId() === android.R.id.home) {
            finish() // close this activity and return to previous activity (if there is any)
        }
        return super.onOptionsItemSelected(item)
    }

    // Updates the toolbar text locale if it set from the android:label property of Manifest
    private fun resetTitle() {
        try {
            val label = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (e: PackageManager.NameNotFoundException) {}
    }
}