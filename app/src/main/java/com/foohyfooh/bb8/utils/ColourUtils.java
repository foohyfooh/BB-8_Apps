package com.foohyfooh.bb8.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class ColourUtils {

    private static final Random random = new Random();
    public static final int[] RED = new int[]{255, 0, 0};
    public static final int[] BLUE = new int[]{0, 0, 255};
    public static final int[] GREEN = new int[]{0, 255, 0};
    public static final int[] PURPLE = new int[]{128, 0, 128};
    public static final int[] PINK = new int[]{255, 192, 203};
    public static final int[] YELLOW = new int[]{255, 255, 0};

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

    public static int[] generateRandomColourArray(){
        return new int[]{random.nextInt(256), random.nextInt(256), random.nextInt(256)};
    }

    public float[] intColoursToFloat(int[] colours) {
        return new float[] {colours[0] / 255f, colours[1] / 255f, colours[2] / 255f};
    }

    private static int ensureInRgbBound(int colour){
        return (int) Math.max(0f, Math.min(colour, 255f));
    }

}
