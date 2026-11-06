package io.github.BushyMustache.centroidFinder;
import java.util.HashMap;
import java.util.Map;

public class Temp {
    public static void main(String[] args) {
        String color = "AARRGGBB";

        System.out.println(0x336699);
        System.out.println(color.substring(2));
    }

    public static double distance(int colorA, int colorB) {
        int[] rgbA = convertToRGB(colorA);
        int[] rgbB = convertToRGB(colorB);

        int redDiff = rgbB[0] - rgbA[0];
        int greenDiff = rgbB[1] - rgbA[1];
        int blueDiff = rgbB[2] - rgbA[2];

        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }

    private static int[] convertToRGB(int color) {
        char[] letters = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        Map<Character, Integer> values = new HashMap<>();
        int num = 0;

        for (char letter : letters) {
            values.put(letter, num);
            num++;
        }

        int[] components = new int[3];
        String colorString = String.format("%06X", color);

        String red = colorString.substring(0, 2);
        String green = colorString.substring(2, 4);
        String blue = colorString.substring(4);

        components[0] = (values.get(red.charAt(0)) * 16) + values.get(red.charAt(1));
        components[1] = (values.get(green.charAt(0)) * 16) + values.get(green.charAt(1));
        components[2] = (values.get(blue.charAt(0)) * 16) + values.get(blue.charAt(1));

        return components;
    }
}
