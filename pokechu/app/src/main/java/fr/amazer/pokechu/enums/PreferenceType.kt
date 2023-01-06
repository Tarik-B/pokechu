package fr.amazer.pokechu.enums

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
        PreferenceType.DISPLAY_ZERO            to Pair("display_zero", false),
    )

    fun getKey(preference: PreferenceType): String {
        return data[preference]!!.first
    }
    fun getDefaultValue(preference: PreferenceType): Any? {
        return data[preference]!!.second
    }
}