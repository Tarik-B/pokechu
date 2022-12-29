package fr.amazer.pokechu.ui.activities

import android.os.Bundle
import fr.amazer.pokechu.BuildConfig
import fr.amazer.pokechu.R
import fr.amazer.pokechu.databinding.ActivityAboutBinding

class ActivityAbout : BaseActivity() {
    private lateinit var binding: ActivityAboutBinding

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

    }
}