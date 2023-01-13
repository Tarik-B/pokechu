package fr.amazer.pokechu.enums

import androidx.appcompat.app.AppCompatDelegate
import java.util.*

enum class PreferenceType {
    SHOW_UNDISCOVERED_INFO,
    SHOW_DISCOVERED_ONLY,
    SHOW_CAPTURED_ONLY,
    LIST_VIEW,
    DATA_LANGUAGE,
    SELECTED_REGION,
    CAPTURED,
    DISCOVERED,
    DISPLAY_ZERO,
    NIGHT_MODE,
    DETAILS_START_VIEW,
}

enum class NightMode {
    OFF,
    ON,
    SYSTEM,
}

object PreferenceData {
    private val data = mapOf(
        PreferenceType.SHOW_UNDISCOVERED_INFO  to Pair("setting_show_undiscovered_info", false),
        PreferenceType.SHOW_DISCOVERED_ONLY    to Pair("setting_show_discovered_only", false),
        PreferenceType.SHOW_CAPTURED_ONLY      to Pair("setting_show_captured_only", false),
        PreferenceType.LIST_VIEW               to Pair("setting_list_view", false),
        PreferenceType.DATA_LANGUAGE           to Pair("setting_data_language", Locale.getDefault().language),
        PreferenceType.SELECTED_REGION         to Pair("setting_selected_region", Region.NATIONAL.ordinal),
        PreferenceType.DISCOVERED              to Pair("pokemon_discovered_", false),
        PreferenceType.CAPTURED                to Pair("pokemon_captured_", false),
        PreferenceType.DISPLAY_ZERO            to Pair("setting_display_zero", false),
        PreferenceType.NIGHT_MODE              to Pair("setting_night_mode", NightMode.SYSTEM.ordinal.toString()),
        PreferenceType.DETAILS_START_VIEW      to Pair("setting_details_start_view", 0),
    )

    private val nightModeToAppCompatMap = mapOf(
        NightMode.OFF to AppCompatDelegate.MODE_NIGHT_NO,
        NightMode.ON to AppCompatDelegate.MODE_NIGHT_YES,
        NightMode.SYSTEM to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    fun getKey(preference: PreferenceType): String {
        return data[preference]!!.first
    }
    fun getDefaultValue(preference: PreferenceType): Any? {
        return data[preference]!!.second
    }
    fun nightModeToAppCompat(value: NightMode): Int {
        return nightModeToAppCompatMap[value]!!
    }
}