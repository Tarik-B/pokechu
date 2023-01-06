package fr.amazer.pokechu.ui.about

import android.os.Bundle
import fr.amazer.pokechu.BuildConfig
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ActivityAboutBinding
import fr.amazer.pokechu.enums.PreferenceType
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.ui.BaseActivity
import fr.amazer.pokechu.utils.UIUtils

class ActivityAbout : BaseActivity() {
    private lateinit var binding: ActivityAboutBinding
    private var clickCount = 0
    private val requiredCount = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.appId = BuildConfig.APPLICATION_ID

        binding.versionName = BuildConfig.VERSION_NAME
        binding.versionCode = BuildConfig.VERSION_CODE.toString()

        binding.buildDate = BuildConfig.BUILD_DATE
        binding.buildTime = BuildConfig.BUILD_TIME

        binding.logo.setOnClickListener { view ->
            // Easter egg
            ++clickCount
            SettingsManager.setSetting(PreferenceType.DISPLAY_ZERO, (clickCount > requiredCount))
            if (clickCount == requiredCount)
                UIUtils.createDefaultParticles(this, view, R.drawable.particle_pokeball, 15, 5000L )
        }
    }
}