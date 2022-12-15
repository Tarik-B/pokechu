package fr.amazer.pokechu

import android.content.Context
import android.util.Log
import com.akexorcist.localizationactivity.ui.LocalizationApplication
import fr.amazer.pokechu.data.*
import fr.amazer.pokechu.managers.DatabaseManager
import fr.amazer.pokechu.managers.SettingsManager
import kotlinx.coroutines.*
import java.util.*

class PokechuApplication: LocalizationApplication() {
    override fun getDefaultLanguage(context: Context) = Locale.ENGLISH

    override fun onCreate() {
        super.onCreate()

        DatabaseManager.with(applicationContext)
        SettingsManager.with(applicationContext)

        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        CoroutineScope(Dispatchers.IO).launch {
            testCoroutine() // coroutine on IO
        }
    }

    suspend fun testCoroutine() {
//        val pokemons: List<Pokemons> = DatabaseManager.getPokemons()
//        val pokemonsByIds: List<Pokemons> = DatabaseManager.getPokemonsByIds(intArrayOf(1, 2, 3, 4, 5))
//        val pokemon: Pokemons = DatabaseManager.getPokemonByName("Tarsal")
//        val regions: List<Regions> = DatabaseManager.getRegions()
//        val regionsByIds: List<Regions> = DatabaseManager.getRegionsByIds(intArrayOf(PokedexType.KANTO.ordinal, PokedexType.PALDEA.ordinal))
//        val region: Regions = DatabaseManager.getRegionByName(PokedexType.PALDEA.name)
//        val pokemonsByRegion: List<Pokemons> = DatabaseManager.getPokemonsByRegion(PokedexType.KANTO.ordinal)
//        val regionsByPokemon: List<Regions> = DatabaseManager.getRegionsByPokemon(1)
        val evolutionRoot: Int = DatabaseManager.findPokemonEvolutionRoot(475)
        Log.i(this::class.simpleName, "evolution root id = ${evolutionRoot}")

        val evolutionChain: List<BaseIdEvolvedIdCondition> = DatabaseManager.findPokemonEvolutions(evolutionRoot)
        Log.i(this::class.simpleName, "evolution chain = ${evolutionChain}")
    }

//    override fun attachBaseContext(base: Context) {
//        SettingsManager.with(base)
//        super.attachBaseContext(base)
//    }


}