package com.example.pokechu_material3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pokechu_material3.databinding.ActivityDetailsBinding
import com.google.android.material.snackbar.Snackbar


class ActivityDetails : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val intent = intent
        val pokemonId = intent.getStringExtra("PokemonId")
        binding.textView.text = pokemonId
    }
}