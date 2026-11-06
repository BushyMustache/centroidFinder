package io.github.BushyMustache.centroidFinder;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

public class JavaCVTest {

    public static void main(String[] args) throws Exception {
        // Test that JavaCV and its native libraries can load.
        FFmpegFrameGrabber.tryLoad();
        System.out.println("JavaCV loaded successfully!");

        if (args.length == 0) {
            System.out.println(
                "Native library test passed. " +
                "Provide a video file path to test video decoding."
            );
            return;
        }

        int frameCount = 0;

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("sampleInput/testVideo.mp4")) {

            grabber.start();

            System.out.println("Video opened successfully!");
            System.out.println("Width: " + grabber.getImageWidth());
            System.out.println("Height: " + grabber.getImageHeight());
            System.out.println("Frame rate: " + grabber.getFrameRate());

            Frame frame;

            while (frameCount < 5
                    && (frame = grabber.grabImage()) != null) {
                frameCount++;

                System.out.println(
                    "Frame " + frameCount + ": "
                    + frame.imageWidth + "x" + frame.imageHeight
                );
            }

            System.out.println(
                "Successfully decoded " + frameCount + " frame(s)."
            );
        }
    }
}
