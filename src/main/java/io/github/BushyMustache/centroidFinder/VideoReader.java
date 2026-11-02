package io.github.BushyMustache.centroidFinder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoReader implements AutoCloseable{
    private final FFmpegFrameGrabber grabber;
    private final Java2DFrameConverter converter;

    private boolean closed = false;

    public VideoReader(String inputPath) throws FFmpegFrameGrabber.Exception {
        grabber = new FFmpegFrameGrabber(inputPath);
        converter = new Java2DFrameConverter();

        try {
            grabber.start();
        } catch (FFmpegFrameGrabber.Exception e) {
            try {
                grabber.release();
            } catch (FFmpegFrameGrabber.Exception cleanupException) {
                e.addSuppressed(cleanupException);
            }

            converter.close();

            throw e;
        }
    }

    public Optional<VideoFrame> readNextFrame() throws FFmpegFrameGrabber.Exception {
        if (closed) {
            throw new IllegalStateException("Cannot read from a closed VideoReader");
        }

        Frame frame;

        while ((frame = grabber.grabImage()) != null) {
            BufferedImage image = converter.convert(frame);

            if (image != null) {
                double timestampSeconds = grabber.getTimestamp() / 1_000_000.0;
                return Optional.of(new VideoFrame(image, timestampSeconds));
            }
        }
        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }

        closed = true;
        IOException cleanupException = null;

        try {
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            cleanupException = new IOException("Failed to stop the video grabber", e);
        }

        try {
            grabber.release();
        } catch (FFmpegFrameGrabber.Exception e) {
            IOException releaseException = new IOException("Failed to release the video grabber", e);

            if (cleanupException == null) {
                cleanupException = releaseException;
            } else {
                cleanupException.addSuppressed(releaseException);
            }
        }

        try {
            converter.close();
        } catch (Exception e) {
            IOException converterException = new IOException("Failed to close the frame converter", e);

            if (cleanupException == null) {
                cleanupException = converterException;
            } else {
                cleanupException.addSuppressed(converterException);
            }
        }

        if (cleanupException != null) {
            throw cleanupException;
        }
    }
}
