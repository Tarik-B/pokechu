package fr.amazer.pokechu

import android.content.Context
import android.util.Log
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.ConditionUtils
import java.util.*

class PokechuApplication: LocalizationApplication() {
    override fun getDefaultLanguage(context: Context) = Locale.ENGLISH

    override fun onCreate() {
        super.onCreate()

//        val data = "OR(AND(AND(FRIENDSHIP)(DAY))(LEVEL_GAIN))(AND(LEVEL_GAIN)(ITEM_HOLD('14')))"
//        val data = "2(1(1(7)(10))(4))(1(4)(6(14)))"
//        val result = ConditionUtils.parseEncodedCondition(data)
//        Log.i("Tag", "${result}")

        DatabaseManager.with(applicationContext)
        SettingsManager.with(applicationContext)
        LocalizationManager.with(applicationContext)
    }

//    override fun attachBaseContext(base: Context) {
//        SettingsManager.with(base)
//        super.attachBaseContext(base)
//    }


}