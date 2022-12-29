package fr.amazer.pokechu.utils

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import fr.amazer.pokechu.enums.EvolutionConditionType
import fr.amazer.pokechu.enums.ItemType
import fr.amazer.pokechu.enums.PokedexType
import fr.amazer.pokechu.enums.PokemonType
import java.io.IOException
import java.io.InputStream

class AssetUtils {
    companion object {

        fun getBitmapFromAsset(assets: AssetManager, strName: String): Bitmap? {
            var istr: InputStream? = null
            try {
                istr = assets.open(strName)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return BitmapFactory.decodeStream(istr)
        }

        fun getPokemonThumbnailPath(pokemon_id: Int): String {
            return "images/pokemons/${pokemon_id}.png"
        }
        fun getTypeThumbnailPath(type: PokemonType): String {
            return "images/types_EV/${type.ordinal}.png"
        }
        fun getTypeThumbnailPathRound(type: PokemonType): String {
            return "images/types_EB/${type.ordinal}.png"
        }
        fun getRegionThumbnailPath(type: PokedexType): String {
            return "images/regions/${type.ordinal}.png"
        }
        fun getItemThumbnailPath(type: ItemType): String {
            return "images/items/${type.ordinal}.png"
        }
        fun getConditionThumbnailPath(type: EvolutionConditionType): String {
            return "images/conditions/${type.ordinal}.png"
        }
    }
}