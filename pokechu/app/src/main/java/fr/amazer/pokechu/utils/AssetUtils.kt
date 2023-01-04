package fr.amazer.pokechu.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import fr.amazer.pokechu.enums.EvolutionCondition
import fr.amazer.pokechu.enums.EvolutionItem
import fr.amazer.pokechu.enums.Region
import fr.amazer.pokechu.enums.PokemonType
import java.io.IOException
import java.io.InputStream

class AssetUtils {
    companion object {

        fun getBitmapFromAsset(context: Context, strName: String): Bitmap? {
            val assetManager = context.assets
            var istr: InputStream? = null
            try {
                istr = assetManager.open(strName)
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
        fun getRegionThumbnailPath(type: Region): String {
            return "images/regions/${type.ordinal}.png"
        }
        fun getItemThumbnailPath(type: EvolutionItem): String {
            return "images/items/${type.ordinal}.png"
        }
        fun getConditionThumbnailPath(type: EvolutionCondition): String {
            return "images/conditions/${type.ordinal}.png"
        }
    }
}