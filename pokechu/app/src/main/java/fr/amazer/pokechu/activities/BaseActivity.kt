package fr.amazer.pokechu.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import com.akexorcist.localizationactivity.ui.LocalizationActivity


open class BaseActivity: LocalizationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resetTitle()
    }

    override fun onAfterLocaleChanged() {
        super.onAfterLocaleChanged()

        resetTitle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle arrow click here
        if (item.itemId == android.R.id.home) {
            // Close this activity and return to previous activity (if there is any)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    // Updates the toolbar text locale if it set from the android:label property of Manifest
    private fun resetTitle() {
        try {
            val label = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes
            if (label != 0) {
                setTitle(label)
            }
        } catch (e: PackageManager.NameNotFoundException) {}
    }
}