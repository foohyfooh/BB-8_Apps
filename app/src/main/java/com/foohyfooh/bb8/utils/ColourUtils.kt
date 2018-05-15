package com.foohyfooh.bb8.utils

import org.apache.commons.lang3.StringUtils

object ColourUtils {

    fun intToHex(colour: Int): String? {
        return StringUtils.leftPad(Integer.toHexString(colour), 2, "0")
    }

    fun extractColoursToArray(colour: String): IntArray {
        val red = colour.substring(1, 3)
        val green = colour.substring(3, 5)
        val blue = colour.substring(5)
        val redValue = ensureInRgbBound(Integer.parseInt(red, 16))
        val greenValue = ensureInRgbBound(Integer.parseInt(green, 16))
        val blueValue = ensureInRgbBound(Integer.parseInt(blue, 16))
        return intArrayOf(redValue, greenValue, blueValue)
    }

    fun intColoursToFloat(colours: IntArray): FloatArray {
        return floatArrayOf(colours[0] / 255f, colours[1] / 255f, colours[2] / 255f)
    }

    private fun ensureInRgbBound(colour: Int): Int {
        return Math.max(0f, Math.min(colour.toFloat(), 255f)).toInt()
    }

}
