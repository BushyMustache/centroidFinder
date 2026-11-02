package io.github.BushyMustache.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VideoReaderTest {

    private Path getTestVideo() throws Exception {
        return Path.of("sampleInput", "moving-square-triangle.mp4");
    }

    @Test
    void readsFirstFrame() throws Exception {

        try (VideoReader reader = new VideoReader(getTestVideo().toString())) {

            Optional<VideoFrame> result = reader.readNextFrame();

            assertTrue(result.isPresent());

            VideoFrame videoFrame = result.get();

            assertNotNull(videoFrame.frame());

            assertTrue(videoFrame.frame().getWidth() > 0);
            assertTrue(videoFrame.frame().getHeight() > 0);

            assertTrue(videoFrame.timeStamp() >= 0);
        }
    }

    @Test
    void readsFramesInOrder() throws Exception {

        try (VideoReader reader = new VideoReader(getTestVideo().toString())) {

            Optional<VideoFrame> result;

            double previousTimestamp = -1;
            int frameCount = 0;

            while ((result = reader.readNextFrame()).isPresent()) {

                VideoFrame videoFrame = result.get();

                assertNotNull(videoFrame.frame());

                assertTrue(videoFrame.timeStamp() >= previousTimestamp,"Timestamps should be non-decreasing");

                previousTimestamp = videoFrame.timeStamp();

                frameCount++;
            }

            assertTrue(frameCount > 1);
        }
    }

    @Test
    void returnsEmptyAtEndOfVideo() throws Exception {

        try (VideoReader reader = new VideoReader(getTestVideo().toString())) {
            int framesRead = 0;

            Optional<VideoFrame> result;

            while (reader.readNextFrame().isPresent()) {
                framesRead++;
            }

            assertTrue(framesRead > 0, "The test video should contain frames");
            assertTrue(reader.readNextFrame().isEmpty());
            assertTrue(reader.readNextFrame().isEmpty());
        }
    }

    @Test
    void invalidPathThrowsException(@TempDir Path tempDir) {

        Path missingVideo = tempDir.resolve("missing.mp4");

        assertFalse(Files.exists(missingVideo));

        assertThrows(FFmpegFrameGrabber.Exception.class, () -> new VideoReader(missingVideo.toString()));
    }

    @Test
    void closesSuccessfully() throws Exception {

        VideoReader reader = new VideoReader(getTestVideo().toString());

        assertDoesNotThrow(reader::close);
    }

    @Test
    void closingTwiceDoesNotThrow() throws Exception {

        VideoReader reader = new VideoReader(getTestVideo().toString());

        reader.close();

        assertDoesNotThrow(reader::close);
    }

    @Test
    void readingAfterCloseThrowsException() throws Exception {

        VideoReader reader = new VideoReader(getTestVideo().toString());

        reader.close();

        assertThrows(IllegalStateException.class, reader::readNextFrame);
    }
}
