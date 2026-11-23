package io.github.BushyMustache.centroidFinder;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class VideoProcessorTest {

    private Path getTestVideo() {
        return Path.of("sampleInput", "moving-square-triangle.mp4");
    }

    @Test
    void processVideoReturnsAnalysisForEveryFrame() throws Exception {
        ImageGroupFinder finder = image -> List.of(new Group(10, new Coordinate(3, 4)));

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        for (FrameAnalysis result : results) {
            assertEquals(3, result.x());
            assertEquals(4, result.y());
        }
    }

    @Test
    void processVideoUsesLargestGroup() throws Exception {
        // The finder contract says groups are sorted descending by size.
        ImageGroupFinder finder = image -> List.of(
            new Group(20, new Coordinate(8, 9)),
            new Group(5, new Coordinate(1, 2))
        );

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        for (FrameAnalysis result : results) {
            assertEquals(8, result.x());
            assertEquals(9, result.y());
        }
    }

    @Test
    void returnsMinusOneWhenNoGroupIsFound() throws Exception {
        ImageGroupFinder finder = image -> List.of();

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        for (FrameAnalysis result : results) {
            assertEquals(-1, result.x());
            assertEquals(-1, result.y());
        }
    }

    @Test
    void resetsCoordinatesWhenCurrentFrameHasNoGroup() throws Exception {
        AtomicInteger frameCounter = new AtomicInteger();

        ImageGroupFinder finder = image -> {
            if (frameCounter.getAndIncrement() == 0) {
                return List.of(new Group(10, new Coordinate(12, 15)));
            }

            return List.of();
        };

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertTrue(results.size() >= 2, "The test video must contain at least two frames");

        // First frame finds the object.
        assertEquals(12, results.get(0).x());
        assertEquals(15, results.get(0).y());

        // The next frame has no group, so coordinates must reset.
        assertEquals(-1, results.get(1).x());
        assertEquals(-1, results.get(1).y());
    }

    @Test
    void timestampsAreNonDecreasing() throws Exception {
        ImageGroupFinder finder = image -> List.of();

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        double previousTimestamp = -1;

        for (FrameAnalysis result : results) {
            assertTrue(result.timestamp() >= previousTimestamp, "Timestamps should be non-decreasing");
            previousTimestamp = result.timestamp();
        }
    }

    @Test
    void processVideoReturnsOneResultPerFrame() throws Exception {
        AtomicInteger frameCounter = new AtomicInteger();

        ImageGroupFinder finder = image -> {
            frameCounter.incrementAndGet();
            return List.of();
        };

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results =
            processor.processVideo(getTestVideo().toString());

        assertEquals(frameCounter.get(), results.size());
    }

    @Test
    void processVideoCallsFinderForEveryFrame() throws Exception {
        AtomicInteger finderCalls = new AtomicInteger();

        ImageGroupFinder finder = image -> {
            finderCalls.incrementAndGet();
            return List.of();
        };

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results =
            processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());
        assertEquals(results.size(), finderCalls.get());
    }

    @Test
    void processVideoUsesCentroidFromSingleGroup() throws Exception {
        ImageGroupFinder finder = image ->
            List.of(new Group(7, new Coordinate(25, 30)));

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results =
            processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        FrameAnalysis first = results.get(0);

        assertEquals(25, first.x());
        assertEquals(30, first.y());
    }

    @Test
    void processVideoPreservesFrameOrder() throws Exception {
        ImageGroupFinder finder = image -> List.of();

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results =
            processor.processVideo(getTestVideo().toString());

        assertTrue(results.size() >= 2);

        for (int i = 1; i < results.size(); i++) {
            assertTrue(
                results.get(i).timestamp() >= results.get(i - 1).timestamp(),
                "Results should preserve frame order"
            );
        }
    }

    @Test
    void processVideoDoesNotReusePreviousCentroid() throws Exception {
        AtomicInteger frameCounter = new AtomicInteger();

        ImageGroupFinder finder = image -> {
            int frame = frameCounter.getAndIncrement();

            if (frame == 0) {
                return List.of(new Group(10, new Coordinate(5, 6)));
            }

            return List.of(new Group(10, new Coordinate(20, 25)));
        };

        VideoProcessor processor = new VideoProcessor(finder);

        List<FrameAnalysis> results =
            processor.processVideo(getTestVideo().toString());

        assertTrue(results.size() >= 2);

        assertEquals(5, results.get(0).x());
        assertEquals(6, results.get(0).y());

        assertEquals(20, results.get(1).x());
        assertEquals(25, results.get(1).y());
    }
}
