package io.github.BushyMustache.centroidFinder;

import java.awt.image.BufferedImage;

/**
 * An implementation of the ImageBinarizer interface that uses color distance
 * to determine whether each pixel should be black or white in the binary image.
 * 
 * The binarization is based on the Euclidean distance between a pixel's color and a reference target color.
 * If the distance is less than the threshold, the pixel is considered white (1);
 * otherwise, it is considered black (0).
 * 
 * The color distance is computed using a provided ColorDistanceFinder, which defines how to compare two colors numerically.
 * The targetColor is represented as a 24-bit RGB integer in the form 0xRRGGBB.
 */
public class DistanceImageBinarizer implements ImageBinarizer {
    private final ColorDistanceFinder distanceFinder;
    private final int threshold;
    private final int targetColor;

    /**
     * Constructs a DistanceImageBinarizer using the given ColorDistanceFinder,
     * target color, and threshold.
     * 
     * The distanceFinder is used to compute the Euclidean distance between a pixel's color and the target color.
     * The targetColor is represented as a 24-bit hex RGB integer (0xRRGGBB).
     * The threshold determines the cutoff for binarization: pixels with distances less than
     * the threshold are marked white, and others are marked black.
     *
     * @param distanceFinder an object that computes the distance between two colors
     * @param targetColor the reference color as a 24-bit hex RGB integer (0xRRGGBB)
     * @param threshold the distance threshold used to decide whether a pixel is white or black
     */
    public DistanceImageBinarizer(ColorDistanceFinder distanceFinder, int targetColor, int threshold) {
        this.distanceFinder = distanceFinder;
        this.targetColor = targetColor;
        this.threshold = threshold;
    }

    /**
     * Converts the given BufferedImage into a binary 2D array using color distance and a threshold.
     * Each entry in the returned array is either 0 or 1, representing a black or white pixel.
     * A pixel is white (1) if its Euclidean distance to the target color is less than the threshold.
     *
     * @param image the input RGB BufferedImage
     * @return a 2D binary array where 1 represents white and 0 represents black
     */
    @Override
    public int[][] toBinaryArray(BufferedImage image) {
        int[][] binaryArray = new int[image.getHeight()][image.getWidth()];

        for (int c = 0; c < image.getHeight(); c++) {
            for (int r = 0; r < image.getWidth(); r++) {
                int color = image.getRGB(r, c);
                double distance = distanceFinder.distance(color, targetColor);
                if ((int) distance < threshold) binaryArray[c][r] = 1;
                else binaryArray[c][r] = 0;
            }
        }

        return binaryArray;
    }

    /**
     * Converts a binary 2D array into a BufferedImage.
     * Each value should be 0 (black) or 1 (white).
     * Black pixels are encoded as 0x000000 and white pixels as 0xFFFFFF.
     *
     * @param image a 2D array of 0s and 1s representing the binary image
     * @return a BufferedImage where black and white pixels are represented with standard RGB hex values
     */
    @Override
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
