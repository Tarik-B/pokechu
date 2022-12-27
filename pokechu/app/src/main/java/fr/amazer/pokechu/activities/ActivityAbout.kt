package fr.amazer.pokechu.activities

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

        binding.textAppId.text = BuildConfig.APPLICATION_ID

        binding.textVersionName.text = BuildConfig.VERSION_NAME
        binding.textVersionCode.text = BuildConfig.VERSION_CODE.toString()

        binding.textBuildDate.text = BuildConfig.BUILD_DATE
        binding.textBuildTime.text = BuildConfig.BUILD_TIME

    }
}