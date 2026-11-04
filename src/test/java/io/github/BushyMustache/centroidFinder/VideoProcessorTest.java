package io.github.BushyMustache.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class VideoProcessorTest {

    private Path getTestVideo() {
        return Path.of("sampleInput", "moving-square-triangle.mp4");
    }

    @Test
    void processVideoReturnsAnalysisForEveryFrame() throws Exception {
        ImageGroupFinder finder = image -> List.of(new Group(10, new Coordinate(3, 4)));

        VideoProcessor processor = new VideoProcessor(finder);

        List<VideoProcessor.FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        for (VideoProcessor.FrameAnalysis result : results) {
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

        List<VideoProcessor.FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        for (VideoProcessor.FrameAnalysis result : results) {
            assertEquals(8, result.x());
            assertEquals(9, result.y());
        }
    }

    @Test
    void returnsMinusOneWhenNoGroupIsFound() throws Exception {
        ImageGroupFinder finder = image -> List.of();

        VideoProcessor processor = new VideoProcessor(finder);

        List<VideoProcessor.FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        for (VideoProcessor.FrameAnalysis result : results) {
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

        List<VideoProcessor.FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

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

        List<VideoProcessor.FrameAnalysis> results = processor.processVideo(getTestVideo().toString());

        assertFalse(results.isEmpty());

        double previousTimestamp = -1;

        for (VideoProcessor.FrameAnalysis result : results) {
            assertTrue(result.timestamp() >= previousTimestamp, "Timestamps should be non-decreasing");
            previousTimestamp = result.timestamp();
        }
    }
}
