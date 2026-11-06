import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Temp {
    public static void main(String[] args){
        // int color = 0x00FF22;
        
        // int[] components = new int[3];
        // String colorString = String.format("%06X", color);

        // String red = colorString.substring(0, 2);
        // String green = colorString.substring(2, 4);
        // String blue = colorString.substring(4);

        // components[0] = (values.get(red.charAt(0)) * 16) + values.get(red.charAt(1));
        // components[1] = (values.get(green.charAt(0)) * 16) + values.get(green.charAt(1));
        // components[2] = (values.get(blue.charAt(0)) * 16) + values.get(blue.charAt(1));

        System.out.println();
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

    public BufferedImage toBufferedImage(int[][] image) {
        int[][] copy = image.clone();
        BufferedImage bufferedImage = new BufferedImage(copy[0].length, copy.length, BufferedImage.TYPE_INT_RGB);
        
        for (int c = 0; c < copy.length; c++) {
            for (int r = 0; r < copy[c].length; r++) {
                if (copy[c][r] == 0) {
                    bufferedImage.setRGB(r, c, 0x000000);
                } else {
                    bufferedImage.setRGB(r, c, 0xFFFFFF);
                }
            }
        }

        return bufferedImage;
    }
}