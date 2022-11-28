package com.example.pokechu_material3

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream

class Utils {
    companion object {

        fun getJsonDataFromAsset(context: Context, fileName: String): String? {
            val jsonString: String
            try {
                jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                return null
            }
            return jsonString
        }

        fun getBitmapFromAsset(assets: AssetManager, strName: String): Bitmap? {
            var istr: InputStream? = null
            try {
                istr = assets.open(strName)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return BitmapFactory.decodeStream(istr)
        }

    }
}