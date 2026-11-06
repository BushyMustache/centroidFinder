package io.github.BushyMustache.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for BinarizingImageGroupFinder.findConnectedGroups.
 *
 * The first section uses stub collaborators to check that the class correctly
 * wires an ImageBinarizer to a BinaryGroupFinder. The second section uses the
 * real collaborators (DistanceImageBinarizer, EuclideanColorDistance and
 * DfsBinaryGroupFinder) to check end-to-end behavior.
 */
public class BinarizingImageGroupFinderTest {

    private static final int BLACK = 0x000000;
    private static final int WHITE = 0xFFFFFF;
    private static final int RED = 0xFF0000;
    private static final int BLUE = 0x0000FF;

    // ---------- stubs ----------

    /** Records how it was called and returns a preset array. */
    private static class StubBinarizer implements ImageBinarizer {
        private final int[][] arrayToReturn;
        private final RuntimeException toThrow;
        int callCount = 0;
        BufferedImage lastImage = null;

        StubBinarizer(int[][] arrayToReturn) {
            this.arrayToReturn = arrayToReturn;
            this.toThrow = null;
        }

        StubBinarizer(RuntimeException toThrow) {
            this.arrayToReturn = null;
            this.toThrow = toThrow;
        }

        @Override
        public int[][] toBinaryArray(BufferedImage image) {
            callCount++;
            lastImage = image;
            if (toThrow != null) {
                throw toThrow;
            }
            return arrayToReturn;
        }

        @Override
        public BufferedImage toBufferedImage(int[][] image) {
            throw new UnsupportedOperationException("not used by BinarizingImageGroupFinder");
        }
    }

    /** Records how it was called and returns a preset list. */
    private static class StubGroupFinder implements BinaryGroupFinder {
        private final List<Group> listToReturn;
        private final RuntimeException toThrow;
        int callCount = 0;
        int[][] lastArray = null;

        StubGroupFinder(List<Group> listToReturn) {
            this.listToReturn = listToReturn;
            this.toThrow = null;
        }

        StubGroupFinder(RuntimeException toThrow) {
            this.listToReturn = null;
            this.toThrow = toThrow;
        }

        @Override
        public List<Group> findConnectedGroups(int[][] image) {
            callCount++;
            lastArray = image;
            if (toThrow != null) {
                throw toThrow;
            }
            return listToReturn;
        }
    }

    // ---------- helpers ----------

    private static Group g(int size, int x, int y) {
        return new Group(size, new Coordinate(x, y));
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

    private static BinarizingImageGroupFinder realFinder(int targetColor, int threshold) {
        return new BinarizingImageGroupFinder(
                new DistanceImageBinarizer(new EuclideanColorDistance(), targetColor, threshold),
                new DfsBinaryGroupFinder());
    }

    // =====================================================================
    // wiring tests (stub collaborators)
    // =====================================================================

    @Test
    public void passesTheImageToTheBinarizer() {
        BufferedImage image = solidImage(2, 2, WHITE);
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{1, 1}, {1, 1}});
        StubGroupFinder groupFinder = new StubGroupFinder(new ArrayList<>());

        new BinarizingImageGroupFinder(binarizer, groupFinder).findConnectedGroups(image);

        assertSame(image, binarizer.lastImage);
    }

    @Test
    public void passesTheBinarizersArrayToTheGroupFinder() {
        int[][] binaryArray = {{1, 0}, {0, 1}};
        StubBinarizer binarizer = new StubBinarizer(binaryArray);
        StubGroupFinder groupFinder = new StubGroupFinder(new ArrayList<>());

        new BinarizingImageGroupFinder(binarizer, groupFinder)
                .findConnectedGroups(solidImage(2, 2, BLACK));

        assertSame(binaryArray, groupFinder.lastArray);
    }

    @Test
    public void returnsTheGroupsFoundByTheGroupFinder() {
        List<Group> groups = List.of(g(3, 1, 0), g(1, 3, 2));
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{1}});
        StubGroupFinder groupFinder = new StubGroupFinder(groups);

        List<Group> result = new BinarizingImageGroupFinder(binarizer, groupFinder)
                .findConnectedGroups(solidImage(1, 1, WHITE));

        assertEquals(groups, result);
    }

    @Test
    public void returnsEmptyListWhenGroupFinderFindsNoGroups() {
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{0, 0}});
        StubGroupFinder groupFinder = new StubGroupFinder(new ArrayList<>());

        List<Group> result = new BinarizingImageGroupFinder(binarizer, groupFinder)
                .findConnectedGroups(solidImage(2, 1, BLACK));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void callsBinarizerAndGroupFinderExactlyOnce() {
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{1}});
        StubGroupFinder groupFinder = new StubGroupFinder(List.of(g(1, 0, 0)));

        new BinarizingImageGroupFinder(binarizer, groupFinder)
                .findConnectedGroups(solidImage(1, 1, WHITE));

        assertEquals(1, binarizer.callCount);
        assertEquals(1, groupFinder.callCount);
    }

    @Test
    public void exceptionFromBinarizerIsPropagated() {
        StubBinarizer binarizer = new StubBinarizer(new IllegalArgumentException("bad image"));
        StubGroupFinder groupFinder = new StubGroupFinder(new ArrayList<>());
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        assertThrows(IllegalArgumentException.class,
                () -> finder.findConnectedGroups(solidImage(1, 1, WHITE)));
        assertEquals(0, groupFinder.callCount);
    }

    @Test
    public void exceptionFromGroupFinderIsPropagated() {
        StubBinarizer binarizer = new StubBinarizer(new int[][] {{1}});
        StubGroupFinder groupFinder = new StubGroupFinder(new IllegalArgumentException("bad array"));
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        assertThrows(IllegalArgumentException.class,
                () -> finder.findConnectedGroups(solidImage(1, 1, WHITE)));
    }

    @Test
    public void doesNotModifyTheImage() {
        int[][] colors = {
            {WHITE, BLACK},
            {0x123456, RED}
        };
        BufferedImage image = imageOf(colors);

        realFinder(WHITE, 10).findConnectedGroups(image);

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                assertEquals(colors[y][x], image.getRGB(x, y) & 0xFFFFFF);
            }
        }
    }

    // =====================================================================
    // end-to-end tests (real collaborators)
    // =====================================================================

    @Test
    public void allBlackImageHasNoGroups() {
        List<Group> result = realFinder(WHITE, 10).findConnectedGroups(solidImage(3, 3, BLACK));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void singleWhitePixelOnBlackBackground() {
        int[][] colors = {
            {BLACK, BLACK, BLACK},
            {BLACK, BLACK, WHITE},
            {BLACK, BLACK, BLACK}
        };
        // pixel at column 2, row 1 -> (x:2, y:1)
        assertEquals(List.of(g(1, 2, 1)), realFinder(WHITE, 10).findConnectedGroups(imageOf(colors)));
    }

    @Test
    public void allWhiteImageIsOneGroup() {
        // 3 wide, 2 tall: x: 6/6 = 1, y: 3/6 = 0
        assertEquals(List.of(g(6, 1, 0)),
                realFinder(WHITE, 10).findConnectedGroups(solidImage(3, 2, WHITE)));
    }

    @Test
    public void twoGroupsAreReturnedLargestFirst() {
        int[][] colors = {
            {WHITE, WHITE, WHITE, BLACK, BLACK},
            {BLACK, BLACK, BLACK, BLACK, BLACK},
            {BLACK, BLACK, BLACK, WHITE, BLACK}
        };
        assertEquals(List.of(g(3, 1, 0), g(1, 3, 2)),
                realFinder(WHITE, 10).findConnectedGroups(imageOf(colors)));
    }

    @Test
    public void diagonalWhitePixelsAreSeparateGroups() {
        int[][] colors = {
            {RED, BLUE},
            {BLUE, RED}
        };
        // target red: the two red pixels touch only diagonally
        assertEquals(List.of(g(1, 1, 1), g(1, 0, 0)),
                realFinder(RED, 50).findConnectedGroups(imageOf(colors)));
    }

    @Test
    public void pixelsWithinThresholdCountAndPixelsOutsideDoNot() {
        // distances from white: 0xFAFAFA ~ 8.7 (inside 20), 0xF0F0F0 ~ 26.0 (outside 20), 0xFFFFFF = 0
        int[][] colors = {{0xFAFAFA, 0xF0F0F0, WHITE}};
        assertEquals(List.of(g(1, 2, 0), g(1, 0, 0)),
                realFinder(WHITE, 20).findConnectedGroups(imageOf(colors)));
    }

    @Test
    public void nonWhiteTargetColorFindsGroupsOfThatColor() {
        int[][] colors = {
            {RED, RED, BLUE},
            {BLUE, BLUE, BLUE}
        };
        // two red pixels at x:0 and x:1 on row 0 -> size 2, x: 1/2 = 0, y: 0
        assertEquals(List.of(g(2, 0, 0)), realFinder(RED, 50).findConnectedGroups(imageOf(colors)));
    }
}
