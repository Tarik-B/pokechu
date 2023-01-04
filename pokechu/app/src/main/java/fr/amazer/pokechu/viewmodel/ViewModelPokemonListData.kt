package fr.amazer.pokechu.viewmodel

import android.graphics.Bitmap
import fr.amazer.pokechu.enums.PokemonType

data class ViewModelPokemonListData(
    val pokemonId: Int,
    val localId: Int,
    val names: Map<String, String>,
    val types: List<PokemonType>,

//    var isDiscovered: Boolean = false,
//    var isCaptured: Boolean = false,
//    var localizedName: String = "",
//    var thumbnailPath: String = "",
//    var typeBitmaps: List<Bitmap>? = null,
)