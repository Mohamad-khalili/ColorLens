package com.compose.colorlens.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.compose.colorlens.models.NamedColor
import org.json.JSONArray
import kotlin.math.pow
import kotlin.math.sqrt


fun hexToRgb(hex: String): IntArray{
    val color = hex.toColorInt()
    return intArrayOf(
        android.graphics.Color.red(color),
        android.graphics.Color.green(color),
        android.graphics.Color.blue(color)
    )
}

fun getContrastTextColor(r: Int, g: Int, b: Int): Color {
    val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
    return if (luminance > 186) Color.Black else Color.White
}

fun loadColorDataset(context: Context): List<NamedColor> {
    val jsonText = context.assets.open("colors1.json").bufferedReader().use { it.readText() }
    val jsonArray = JSONArray(jsonText)
    val colors = mutableListOf<NamedColor>()

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val name = obj.getString("name")
        val hex = obj.getString("hex")
        val rgb = hexToRgb(hex)
        colors.add(NamedColor(name, hex, rgb[0], rgb[1], rgb[2]))
    }
    return colors
}



fun findNearestColor(pixelColor: Int, colorDataset: List<NamedColor>): NamedColor {
    val r1 = android.graphics.Color.red(pixelColor)
    val g1 = android.graphics.Color.green(pixelColor)
    val b1 = android.graphics.Color.blue(pixelColor)

    return colorDataset.minByOrNull { color ->
        sqrt(
            (r1 - color.r).toDouble().pow(2.0) +
                    (g1 - color.g).toDouble().pow(2.0) +
                    (b1 - color.b).toDouble().pow(2.0)
        )
    } ?: NamedColor("Unknown", "#000000", 0, 0, 0)
}

