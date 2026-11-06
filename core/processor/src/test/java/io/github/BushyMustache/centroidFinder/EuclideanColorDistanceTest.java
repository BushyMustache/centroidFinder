package io.github.BushyMustache.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for EuclideanColorDistance.distance.
 */
public class EuclideanColorDistanceTest {

    private static final double DELTA = 1e-9;

    private ColorDistanceFinder finder;

    @BeforeEach
    public void setUp() {
        finder = new EuclideanColorDistance();
    }

    // ---------- identical colors ----------

    @Test
    public void identicalBlackIsZero() {
        assertEquals(0.0, finder.distance(0x000000, 0x000000), DELTA);
    }

    @Test
    public void identicalWhiteIsZero() {
        assertEquals(0.0, finder.distance(0xFFFFFF, 0xFFFFFF), DELTA);
    }

    @Test
    public void identicalArbitraryColorIsZero() {
        assertEquals(0.0, finder.distance(0x123456, 0x123456), DELTA);
    }

    // ---------- single-channel differences ----------

    @Test
    public void redOnlyDifference() {
        assertEquals(255.0, finder.distance(0xFF0000, 0x000000), DELTA);
    }

    @Test
    public void greenOnlyDifference() {
        assertEquals(255.0, finder.distance(0x00FF00, 0x000000), DELTA);
    }

    @Test
    public void blueOnlyDifference() {
        assertEquals(255.0, finder.distance(0x0000FF, 0x000000), DELTA);
    }

    @Test
    public void smallestPossibleDifferenceInBlueIsOne() {
        assertEquals(1.0, finder.distance(0x000001, 0x000000), DELTA);
    }

    @Test
    public void smallestPossibleDifferenceInRedIsOne() {
        assertEquals(1.0, finder.distance(0xFFFFFF, 0xFEFFFF), DELTA);
    }

    @Test
    public void sharedNonZeroChannelsDoNotAffectDistance() {
        // only blue differs by 16
        assertEquals(16.0, finder.distance(0xFF8000, 0xFF8010), DELTA);
    }

    // ---------- multi-channel differences ----------

    @Test
    public void blackToWhiteIs255TimesRootThree() {
        assertEquals(255.0 * Math.sqrt(3), finder.distance(0x000000, 0xFFFFFF), DELTA);
    }

    @Test
    public void redToGreenIs255TimesRootTwo() {
        assertEquals(255.0 * Math.sqrt(2), finder.distance(0xFF0000, 0x00FF00), DELTA);
    }

    @Test
    public void redToBlueIs255TimesRootTwo() {
        assertEquals(255.0 * Math.sqrt(2), finder.distance(0xFF0000, 0x0000FF), DELTA);
    }

    @Test
    public void threeFourFiveTriangle() {
        // red differs by 3, green by 4, blue by 0 -> 5
        assertEquals(5.0, finder.distance(0x030400, 0x000000), DELTA);
    }

    @Test
    public void oneTwoThreeDifferences() {
        assertEquals(Math.sqrt(14), finder.distance(0x010203, 0x000000), DELTA);
    }

    @Test
    public void equalDifferenceInAllChannels() {
        // each channel differs by 0x30 = 48
        assertEquals(48.0 * Math.sqrt(3), finder.distance(0x102030, 0x405060), DELTA);
    }

    @Test
    public void mediumGrayToBlack() {
        assertEquals(128.0 * Math.sqrt(3), finder.distance(0x808080, 0x000000), DELTA);
    }

    @Test
    public void channelsAreSeparatedCorrectly() {
        // red differs by 1, green by 255 (no borrowing between channels)
        assertEquals(Math.sqrt(1 + 255 * 255), finder.distance(0x00FF00, 0x010000), DELTA);
    }

    // ---------- properties ----------

    @Test
    public void distanceIsSymmetric() {
        int a = 0x1A2B3C;
        int b = 0xC3B2A1;
        assertEquals(finder.distance(a, b), finder.distance(b, a), DELTA);
    }

    @Test
    public void distanceIsNeverNegativeWhenSecondColorIsLarger() {
        double d = finder.distance(0x000000, 0xFF0000);
        assertTrue(d >= 0);
        assertEquals(255.0, d, DELTA);
    }

    @Test
    public void triangleInequalityHolds() {
        int a = 0x102030;
        int b = 0x80A0C0;
        int c = 0xF0E0D0;
        assertTrue(finder.distance(a, c) <= finder.distance(a, b) + finder.distance(b, c) + DELTA);
    }

    @Test
    public void resultIsFiniteForExtremeColors() {
        double d = finder.distance(0x000000, 0xFFFFFF);
        assertFalse(Double.isNaN(d));
        assertFalse(Double.isInfinite(d));
    }
}
