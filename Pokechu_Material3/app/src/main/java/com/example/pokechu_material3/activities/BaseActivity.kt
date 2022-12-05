package com.example.pokechu_material3.activities

import com.akexorcist.localizationactivity.ui.LocalizationActivity

open class BaseActivity: LocalizationActivity() { //You can use your preferred activity instead of AppCompatActivity

     // Updates the toolbar text locale if it set from the android:label property of Manifest
//    private fun resetTitle() {
//        try {
//            val label = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes;
//            if (label != 0) {
//                setTitle(label);
//            }
//        } catch (e: PackageManager.NameNotFoundException) {}
//    }
}