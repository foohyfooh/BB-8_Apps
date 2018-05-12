package com.foohyfooh.bb8.utils;

import org.apache.commons.lang3.StringUtils;

public class ColourUtils {

    public static String intToHex(int colour){
        return StringUtils.leftPad(Integer.toHexString(colour), 2, "0");
    }

    public static int[] extractColoursToArray(String colour){
        String red = colour.substring(1, 3);
        String green = colour.substring(3, 5);
        String blue = colour.substring(5);
        int redValue = ensureInRgbBound(Integer.parseInt(red, 16));
        int greenValue = ensureInRgbBound(Integer.parseInt(green, 16));
        int blueValue = ensureInRgbBound(Integer.parseInt(blue, 16));
        return new int[] {redValue, greenValue, blueValue};
    }

    private static int ensureInRgbBound(int colour){
        return (int) Math.max(0f, Math.min(colour, 255f));
    }

}
