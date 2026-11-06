package io.github.BushyMustache.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for DistanceImageBinarizer.toBinaryArray and toBufferedImage.
 */
public class DistanceImageBinarizerTest {

    private static final int BLACK = 0x000000;
    private static final int WHITE = 0xFFFFFF;

    // ---------- helpers ----------

    private static DistanceImageBinarizer binarizer(int targetColor, int threshold) {
        return new DistanceImageBinarizer(new EuclideanColorDistance(), targetColor, threshold);
    }

    /** Builds an RGB image from rows of 0xRRGGBB colors (row = y, column = x). */
    private static BufferedImage imageOf(int[][] colors) {
        int height = colors.length;
        int width = colors[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, colors[y][x]);
            }
        }
        return image;
    }

    private static BufferedImage solidImage(int width, int height, int color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, color);
            }
        }
        return image;
    }

    private static int rgbAt(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    private static int[][] copyOf(int[][] array) {
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i].clone();
        }
        return copy;
    }

    // =====================================================================
    // toBinaryArray
    // =====================================================================

    @Test
    public void toBinaryArrayHasRowsEqualToHeightAndColumnsEqualToWidth() {
        BufferedImage image = solidImage(3, 2, BLACK); // width 3, height 2
        int[][] result = binarizer(WHITE, 10).toBinaryArray(image);
        assertEquals(2, result.length);
        assertEquals(3, result[0].length);
        assertEquals(3, result[1].length);
    }

    @Test
    public void toBinaryArraySinglePixelMatchingTargetIsWhite() {
        int[][] result = binarizer(0x336699, 10).toBinaryArray(solidImage(1, 1, 0x336699));
        assertArrayEquals(new int[][] {{1}}, result);
    }

    @Test
    public void toBinaryArraySinglePixelFarFromTargetIsBlack() {
        int[][] result = binarizer(WHITE, 10).toBinaryArray(solidImage(1, 1, BLACK));
        assertArrayEquals(new int[][] {{0}}, result);
    }

    @Test
    public void toBinaryArrayDistanceJustBelowThresholdIsWhite() {
        // distance from black to 0x000004 is 4, threshold 5
        int[][] result = binarizer(BLACK, 5).toBinaryArray(solidImage(1, 1, 0x000004));
        assertArrayEquals(new int[][] {{1}}, result);
    }

    @Test
    public void toBinaryArrayDistanceEqualToThresholdIsBlack() {
        // distance from black to 0x000005 is exactly 5, and "less than" is strict
        int[][] result = binarizer(BLACK, 5).toBinaryArray(solidImage(1, 1, 0x000005));
        assertArrayEquals(new int[][] {{0}}, result);
    }

    @Test
    public void toBinaryArrayDistanceJustAboveThresholdIsBlack() {
        int[][] result = binarizer(BLACK, 5).toBinaryArray(solidImage(1, 1, 0x000006));
        assertArrayEquals(new int[][] {{0}}, result);
    }

    @Test
    public void toBinaryArrayUsesFullThreeChannelDistance() {
        // red differs by 3, green by 4 -> distance exactly 5
        BufferedImage image = solidImage(1, 1, 0x030400);
        assertArrayEquals(new int[][] {{1}}, binarizer(BLACK, 6).toBinaryArray(image));
        assertArrayEquals(new int[][] {{0}}, binarizer(BLACK, 5).toBinaryArray(image));
    }

    @Test
    public void toBinaryArrayThresholdZeroMakesEverythingBlack() {
        // even a pixel identical to the target has distance 0, which is not < 0
        int[][] result = binarizer(0x336699, 0).toBinaryArray(solidImage(2, 2, 0x336699));
        assertArrayEquals(new int[][] {{0, 0}, {0, 0}}, result);
    }

    @Test
    public void toBinaryArrayAllPixelsMatchingGivesAllOnes() {
        int[][] result = binarizer(0xFF0000, 1).toBinaryArray(solidImage(3, 3, 0xFF0000));
        assertArrayEquals(new int[][] {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, result);
    }

    @Test
    public void toBinaryArrayNoPixelsMatchingGivesAllZeros() {
        int[][] result = binarizer(0xFF0000, 1).toBinaryArray(solidImage(3, 2, 0x0000FF));
        assertArrayEquals(new int[][] {{0, 0, 0}, {0, 0, 0}}, result);
    }

    @Test
    public void toBinaryArrayPlacesPixelsAtRowYColumnX() {
        // white target pixel at (x:2, y:0) in a 3-wide, 2-tall image
        int[][] colors = {
            {BLACK, BLACK, WHITE},
            {BLACK, BLACK, BLACK}
        };
        int[][] result = binarizer(WHITE, 10).toBinaryArray(imageOf(colors));
        assertArrayEquals(new int[][] {{0, 0, 1}, {0, 0, 0}}, result);
    }

    @Test
    public void toBinaryArrayMixedImage() {
        int[][] colors = {
            {WHITE, BLACK, WHITE},
            {BLACK, WHITE, BLACK},
            {WHITE, WHITE, BLACK}
        };
        int[][] expected = {
            {1, 0, 1},
            {0, 1, 0},
            {1, 1, 0}
        };
        assertArrayEquals(expected, binarizer(WHITE, 10).toBinaryArray(imageOf(colors)));
    }

    @Test
    public void toBinaryArrayWorksWithNonBlackNonWhiteTarget() {
        int[][] colors = {
            {0xFF0000, 0xF00000, 0x0000FF}
        };
        // distance to pure red: 0, 15, ~360.6 ; threshold 20
        int[][] result = binarizer(0xFF0000, 20).toBinaryArray(imageOf(colors));
        assertArrayEquals(new int[][] {{1, 1, 0}}, result);
    }

    @Test
    public void toBinaryArrayLargerThresholdAcceptsMoreColors() {
        int[][] colors = {{BLACK, 0x646464, WHITE}};
        // distances from black: 0, ~173.2, ~441.7
        assertArrayEquals(new int[][] {{1, 0, 0}}, binarizer(BLACK, 100).toBinaryArray(imageOf(colors)));
        assertArrayEquals(new int[][] {{1, 1, 0}}, binarizer(BLACK, 200).toBinaryArray(imageOf(colors)));
        assertArrayEquals(new int[][] {{1, 1, 1}}, binarizer(BLACK, 500).toBinaryArray(imageOf(colors)));
    }

    @Test
    public void toBinaryArrayUsesTheProvidedDistanceFinderResult() {
        BufferedImage image = solidImage(2, 2, 0x123456);

        ColorDistanceFinder alwaysZero = new ColorDistanceFinder() {
            @Override
            public double distance(int colorA, int colorB) {
                return 0.0;
            }
        };
        ColorDistanceFinder alwaysFar = new ColorDistanceFinder() {
            @Override
            public double distance(int colorA, int colorB) {
                return 1000.0;
            }
        };

        int[][] allWhite = new DistanceImageBinarizer(alwaysZero, 0xABCDEF, 1).toBinaryArray(image);
        int[][] allBlack = new DistanceImageBinarizer(alwaysFar, 0xABCDEF, 1).toBinaryArray(image);

        assertArrayEquals(new int[][] {{1, 1}, {1, 1}}, allWhite);
        assertArrayEquals(new int[][] {{0, 0}, {0, 0}}, allBlack);
    }

    @Test
    public void toBinaryArrayDoesNotModifyTheImage() {
        int[][] colors = {
            {WHITE, BLACK},
            {0x123456, 0xFF0000}
        };
        BufferedImage image = imageOf(colors);
        binarizer(WHITE, 10).toBinaryArray(image);
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                assertEquals(colors[y][x], rgbAt(image, x, y));
            }
        }
    }

    // =====================================================================
    // toBufferedImage
    // =====================================================================

    @Test
    public void toBufferedImageHasWidthEqualToColumnsAndHeightEqualToRows() {
        int[][] array = {
            {0, 1, 0},
            {1, 0, 1}
        };
        BufferedImage image = binarizer(WHITE, 10).toBufferedImage(array);
        assertEquals(3, image.getWidth());
        assertEquals(2, image.getHeight());
    }

    @Test
    public void toBufferedImageSingleBlackPixel() {
        BufferedImage image = binarizer(WHITE, 10).toBufferedImage(new int[][] {{0}});
        assertEquals(1, image.getWidth());
        assertEquals(1, image.getHeight());
        assertEquals(BLACK, rgbAt(image, 0, 0));
    }

    @Test
    public void toBufferedImageSingleWhitePixel() {
        BufferedImage image = binarizer(WHITE, 10).toBufferedImage(new int[][] {{1}});
        assertEquals(WHITE, rgbAt(image, 0, 0));
    }

    @Test
    public void toBufferedImageAllZerosIsAllBlack() {
        BufferedImage image = binarizer(WHITE, 10).toBufferedImage(new int[][] {{0, 0}, {0, 0}});
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                assertEquals(BLACK, rgbAt(image, x, y));
            }
        }
    }

    @Test
    public void toBufferedImageAllOnesIsAllWhite() {
        BufferedImage image = binarizer(WHITE, 10).toBufferedImage(new int[][] {{1, 1, 1}, {1, 1, 1}});
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                assertEquals(WHITE, rgbAt(image, x, y));
            }
        }
    }

    @Test
    public void toBufferedImagePlacesRowAsYAndColumnAsX() {
        int[][] array = {
            {1, 0, 0},
            {0, 0, 1}
        };
        BufferedImage image = binarizer(WHITE, 10).toBufferedImage(array);
        assertEquals(WHITE, rgbAt(image, 0, 0)); // row 0, column 0
        assertEquals(BLACK, rgbAt(image, 1, 0));
        assertEquals(BLACK, rgbAt(image, 2, 0));
        assertEquals(BLACK, rgbAt(image, 0, 1));
        assertEquals(BLACK, rgbAt(image, 1, 1));
        assertEquals(WHITE, rgbAt(image, 2, 1)); // row 1, column 2
    }

    @Test
    public void toBufferedImageCheckerboard() {
        int[][] array = {
            {1, 0},
            {0, 1}
        };
        BufferedImage image = binarizer(WHITE, 10).toBufferedImage(array);
        assertEquals(WHITE, rgbAt(image, 0, 0));
        assertEquals(BLACK, rgbAt(image, 1, 0));
        assertEquals(BLACK, rgbAt(image, 0, 1));
        assertEquals(WHITE, rgbAt(image, 1, 1));
    }

    @Test
    public void toBufferedImageOnlyUsesPureBlackAndPureWhite() {
        int[][] array = {
            {1, 0, 1, 1},
            {0, 0, 1, 0},
            {1, 1, 0, 0}
        };
        BufferedImage image = binarizer(WHITE, 10).toBufferedImage(array);
        for (int y = 0; y < array.length; y++) {
            for (int x = 0; x < array[y].length; x++) {
                int expected = array[y][x] == 1 ? WHITE : BLACK;
                assertEquals(expected, rgbAt(image, x, y));
            }
        }
    }

    @Test
    public void toBufferedImageDoesNotModifyTheArray() {
        int[][] array = {
            {1, 0, 1},
            {0, 1, 0}
        };
        int[][] copy = copyOf(array);
        binarizer(WHITE, 10).toBufferedImage(array);
        assertArrayEquals(copy, array);
    }

    // =====================================================================
    // round trip
    // =====================================================================

    @Test
    public void binaryArrayToImageAndBackIsUnchanged() {
        int[][] array = {
            {1, 0, 0, 1},
            {0, 1, 1, 0},
            {1, 1, 0, 0}
        };
        // target white, small threshold: white pixels -> 1, black pixels -> 0
        DistanceImageBinarizer binarizer = binarizer(WHITE, 10);
        int[][] roundTrip = binarizer.toBinaryArray(binarizer.toBufferedImage(array));
        assertArrayEquals(array, roundTrip);
    }
}
