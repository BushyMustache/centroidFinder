package io.github.BushyMustache.centroidFinder;

import java.util.HashMap;
import java.util.Map;

public class EuclideanColorDistance implements ColorDistanceFinder {
    /**
     * Returns the euclidean color distance between two hex RGB colors.
     * 
     * Each color is represented as a 24-bit integer in the form 0xRRGGBB, where
     * RR is the red component, GG is the green component, and BB is the blue component,
     * each ranging from 0 to 255.
     * 
     * The Euclidean color distance is calculated by treating each color as a point
     * in 3D space (red, green, blue) and applying the Euclidean distance formula:
     * 
     * sqrt((r1 - r2)^2 + (g1 - g2)^2 + (b1 - b2)^2)
     * 
     * This gives a measure of how visually different the two colors are.
     * 
     * @param colorA the first color as a 24-bit hex RGB integer
     * @param colorB the second color as a 24-bit hex RGB integer
     * @return the Euclidean distance between the two colors
     */
    @Override
    public double distance(int colorA, int colorB) {
        int[] rgbA = convertToRGB(colorA);
        int[] rgbB = convertToRGB(colorB);

        int redDiff = rgbB[0] - rgbA[0];
        int greenDiff = rgbB[1] - rgbA[1];
        int blueDiff = rgbB[2] - rgbA[2];

        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }

    private int[] convertToRGB(int color) {
        char[] letters = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        Map<Character, Integer> values = new HashMap<>();
        int num = 0;

        for (char letter : letters) {
            values.put(letter, num);
            num++;
        }

        int[] components = new int[3];
        String colorString = String.format("%06X", color);
        if (colorString.length() == 8) colorString = colorString.substring(2);

        String red = colorString.substring(0, 2);
        String green = colorString.substring(2, 4);
        String blue = colorString.substring(4);

        components[0] = (values.get(red.charAt(0)) * 16) + values.get(red.charAt(1));
        components[1] = (values.get(green.charAt(0)) * 16) + values.get(green.charAt(1));
        components[2] = (values.get(blue.charAt(0)) * 16) + values.get(blue.charAt(1));

        return components;
    }
}
