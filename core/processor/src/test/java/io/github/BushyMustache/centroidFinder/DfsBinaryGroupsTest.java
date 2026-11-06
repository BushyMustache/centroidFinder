package io.github.BushyMustache.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for BinaryGroupFinder.findConnectedGroups.
 *
 * Replace the body of newFinder() with your own implementation class.
 *
 * NOTE: The interface Javadoc says ties are broken by "descending y, then
 * descending x", but Group.compareTo compares size, then x, then y (so a
 * descending sort breaks ties by x first, then y). These tests only use ties
 * that differ in a single dimension, or compare unordered, so they pass under
 * either reading.
 */
public class DfsBinaryGroupsTest {

    // ---------- setup / helpers ----------

    private BinaryGroupFinder newFinder() {
        return new DfsBinaryGroupFinder();
    }

    private List<Group> run(int[][] image) {
        return newFinder().findConnectedGroups(image);
    }

    private static Group g(int size, int x, int y) {
        return new Group(size, new Coordinate(x, y));
    }

    private static void assertDescending(List<Group> groups) {
        for (int i = 0; i < groups.size() - 1; i++) {
            assertTrue(groups.get(i).compareTo(groups.get(i + 1)) >= 0,
                    "Groups at index " + i + " and " + (i + 1) + " are not in descending order");
        }
    }

    private static void assertSameGroupsAnyOrder(List<Group> expected, List<Group> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
    }

    // ---------- exceptions ----------

    @Test
    public void nullImageThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> run(null));
    }

    @Test
    public void nullRowInMiddleThrowsNullPointerException() {
        int[][] image = {{1, 0}, null, {0, 1}};
        assertThrows(NullPointerException.class, () -> run(image));
    }

    @Test
    public void nullFirstRowThrowsNullPointerException() {
        int[][] image = {null, {1, 0}};
        assertThrows(NullPointerException.class, () -> run(image));
    }

    @Test
    public void nullLastRowThrowsNullPointerException() {
        int[][] image = {{1, 0}, null};
        assertThrows(NullPointerException.class, () -> run(image));
    }

    @Test
    public void emptyArrayThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> run(new int[0][0]));
    }

    @Test
    public void emptyRowsThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> run(new int[3][0]));
    }

    @Test
    public void laterRowLongerThrowsIllegalArgumentException() {
        int[][] image = {{1, 0}, {1, 0, 1}};
        assertThrows(IllegalArgumentException.class, () -> run(image));
    }

    @Test
    public void laterRowShorterThrowsIllegalArgumentException() {
        int[][] image = {{1, 0, 1}, {1, 0}};
        assertThrows(IllegalArgumentException.class, () -> run(image));
    }

    @Test
    public void valueGreaterThanOneThrowsIllegalArgumentException() {
        int[][] image = {{1, 0}, {0, 2}};
        assertThrows(IllegalArgumentException.class, () -> run(image));
    }

    @Test
    public void negativeValueThrowsIllegalArgumentException() {
        int[][] image = {{-1, 1}, {0, 1}};
        assertThrows(IllegalArgumentException.class, () -> run(image));
    }

    // ---------- no groups ----------

    @Test
    public void allZerosReturnsEmptyList() {
        List<Group> result = run(new int[][] {{0, 0, 0}, {0, 0, 0}});
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void singleZeroReturnsEmptyList() {
        List<Group> result = run(new int[][] {{0}});
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---------- single group ----------

    @Test
    public void singlePixelImage() {
        assertEquals(List.of(g(1, 0, 0)), run(new int[][] {{1}}));
    }

    @Test
    public void singlePixelUsesXAsColumnAndYAsRow() {
        int[][] image = {
            {0, 0, 0},
            {0, 0, 1},
            {0, 0, 0}
        };
        // row:1, column:2 -> (x:2, y:1)
        assertEquals(List.of(g(1, 2, 1)), run(image));
    }

    @Test
    public void singlePixelInBottomLeftCorner() {
        int[][] image = {
            {0, 0, 0},
            {0, 0, 0},
            {1, 0, 0}
        };
        assertEquals(List.of(g(1, 0, 2)), run(image));
    }

    @Test
    public void singlePixelInTopRightCorner() {
        int[][] image = {
            {0, 0, 1},
            {0, 0, 0},
            {0, 0, 0}
        };
        assertEquals(List.of(g(1, 2, 0)), run(image));
    }

    @Test
    public void fullTwoByTwoBlock() {
        int[][] image = {
            {1, 1},
            {1, 1}
        };
        // x: 2/4 = 0, y: 2/4 = 0
        assertEquals(List.of(g(4, 0, 0)), run(image));
    }

    @Test
    public void horizontalLine() {
        // x: (0+1+2+3)/4 = 1
        assertEquals(List.of(g(4, 1, 0)), run(new int[][] {{1, 1, 1, 1}}));
    }

    @Test
    public void verticalLine() {
        // y: (0+1+2+3)/4 = 1
        assertEquals(List.of(g(4, 0, 1)), run(new int[][] {{1}, {1}, {1}, {1}}));
    }

    @Test
    public void wideRectangleFullOfOnes() {
        int[][] image = {
            {1, 1, 1, 1},
            {1, 1, 1, 1}
        };
        // x: 12/8 = 1, y: 4/8 = 0
        assertEquals(List.of(g(8, 1, 0)), run(image));
    }

    @Test
    public void tallRectangleFullOfOnes() {
        int[][] image = {
            {1, 1},
            {1, 1},
            {1, 1},
            {1, 1}
        };
        // x: 4/8 = 0, y: 12/8 = 1
        assertEquals(List.of(g(8, 0, 1)), run(image));
    }

    @Test
    public void allOnesFiveByFive() {
        int[][] image = new int[5][5];
        for (int[] row : image) {
            Arrays.fill(row, 1);
        }
        // x: 50/25 = 2, y: 2
        assertEquals(List.of(g(25, 2, 2)), run(image));
    }

    @Test
    public void largeImageAllOnes() {
        int[][] image = new int[84][84];
        for (int[] row : image) {
            Arrays.fill(row, 1);
        }
        // x: 495000/10000 = 49, y: 49
        assertEquals(List.of(g(7056, 41, 41)), run(image));
    }

    // ---------- integer division ----------

    @Test
    public void xCentroidUsesIntegerDivision() {
        // x: (1+2)/2 = 1 (not 1.5, not 2)
        assertEquals(List.of(g(2, 1, 0)), run(new int[][] {{0, 1, 1}}));
    }

    @Test
    public void yCentroidUsesIntegerDivision() {
        // y: (1+2)/2 = 1
        assertEquals(List.of(g(2, 0, 1)), run(new int[][] {{0}, {1}, {1}}));
    }

    @Test
    public void xCentroidWithOffsetTruncates() {
        // x: (2+3+4+5)/4 = 14/4 = 3
        assertEquals(List.of(g(4, 3, 0)), run(new int[][] {{0, 0, 1, 1, 1, 1}}));
    }

    @Test
    public void lShapedGroupTruncatesBothDimensions() {
        int[][] image = {
            {1, 1},
            {1, 0}
        };
        // pixels (0,0),(1,0),(0,1): x: 1/3 = 0, y: 1/3 = 0
        assertEquals(List.of(g(3, 0, 0)), run(image));
    }

    // ---------- connectivity ----------

    @Test
    public void diagonalPixelsAreNotConnected() {
        int[][] image = {
            {1, 0},
            {0, 1}
        };
        assertEquals(List.of(g(1, 1, 1), g(1, 0, 0)), run(image));
    }

    @Test
    public void antiDiagonalPixelsAreNotConnected() {
        int[][] image = {
            {0, 1},
            {1, 0}
        };
        // order of these two depends on tie-break reading, so compare unordered
        assertSameGroupsAnyOrder(List.of(g(1, 1, 0), g(1, 0, 1)), run(image));
    }

    @Test
    public void diagonalStaircaseGivesOneGroupPerPixel() {
        int[][] image = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
        assertEquals(List.of(g(1, 2, 2), g(1, 1, 1), g(1, 0, 0)), run(image));
    }

    @Test
    public void uShapedGroupIsSingleGroup() {
        int[][] image = {
            {1, 0, 1},
            {1, 0, 1},
            {1, 1, 1}
        };
        // x: (0*3 + 2*3 + 1)/7 = 1, y: (3 + 3 + 2)/7 = 1
        assertEquals(List.of(g(7, 1, 1)), run(image));
    }

    @Test
    public void ringWithEmptyCenterIsSingleGroup() {
        int[][] image = {
            {1, 1, 1},
            {1, 0, 1},
            {1, 1, 1}
        };
        assertEquals(List.of(g(8, 1, 1)), run(image));
    }

    @Test
    public void snakeShapedGroupIsSingleGroup() {
        int[][] image = {
            {1, 1, 1},
            {0, 0, 1},
            {1, 1, 1},
            {1, 0, 0},
            {1, 1, 1}
        };
        // 11 pixels; x sum = 3+2+3+0+3 = 11 -> 1; y sum = 0+1*1+2*3+3*1+4*3 = 22 -> 2
        assertEquals(List.of(g(11, 1, 2)), run(image));
    }

    // ---------- multiple groups and ordering ----------

    @Test
    public void largerGroupComesFirst() {
        int[][] image = {
            {1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0}
        };
        assertEquals(List.of(g(3, 1, 0), g(1, 3, 2)), run(image));
    }

    @Test
    public void smallerGroupFirstInImageStillSortsLast() {
        int[][] image = {{1, 0, 1, 1}};
        assertEquals(List.of(g(2, 2, 0), g(1, 0, 0)), run(image));
    }

    @Test
    public void equalSizeTiesBrokenByDescendingX() {
        int[][] image = {{1, 0, 1}};
        assertEquals(List.of(g(1, 2, 0), g(1, 0, 0)), run(image));
    }

    @Test
    public void equalSizeAndXTiesBrokenByDescendingY() {
        int[][] image = {{1}, {0}, {1}};
        assertEquals(List.of(g(1, 0, 2), g(1, 0, 0)), run(image));
    }

    @Test
    public void threeEqualSizedGroupsOrderedByDescendingX() {
        int[][] image = {
            {1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1},
            {0, 0, 0, 0, 0}
        };
        assertEquals(List.of(g(2, 4, 0), g(2, 2, 0), g(2, 0, 0)), run(image));
    }

    @Test
    public void groupsSeparatedByZeroRowAreDistinctAndOrderedByY() {
        int[][] image = {
            {1, 1},
            {0, 0},
            {1, 1}
        };
        assertEquals(List.of(g(2, 0, 2), g(2, 0, 0)), run(image));
    }

    @Test
    public void manyGroupsOfVariousSizesAreSortedDescending() {
        int[][] image = {
            {1, 0, 1, 1, 0, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 0, 0},
            {1, 1, 0, 0, 0, 1, 0, 0}
        };
        List<Group> expected = List.of(
                g(4, 1, 2),   // (0..3, 2)
                g(3, 6, 0),   // (5..7, 0): x = 18/3
                g(2, 5, 3),   // (5,3),(5,4): y = 7/2
                g(2, 2, 0),   // (2,0),(3,0): x = 5/2
                g(2, 0, 4),   // (0,4),(1,4): x = 1/2
                g(1, 0, 0));  // (0,0)
        assertEquals(expected, run(image));
    }

    @Test
    public void checkerboardGivesOneGroupPerPixelInDescendingOrder() {
        int[][] image = {
            {1, 0, 1, 0},
            {0, 1, 0, 1},
            {1, 0, 1, 0},
            {0, 1, 0, 1}
        };
        List<Group> result = run(image);
        assertEquals(8, result.size());
        for (Group group : result) {
            assertEquals(1, group.size());
        }
        assertEquals(8, new HashSet<>(result).size());
        assertDescending(result);
    }

    @Test
    public void mixedImageWithTwoGroups() {
        int[][] image = {
            {1, 1, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
        // (0,0),(1,0),(1,1): x: 2/3 = 0, y: 1/3 = 0
        assertEquals(List.of(g(3, 0, 0), g(1, 2, 2)), run(image));
    }
}
