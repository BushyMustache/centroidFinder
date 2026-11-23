package io.github.BushyMustache.centroidFinder;

import java.awt.image.BufferedImage;

/**
 * Represents a singular frame of a video.
 * Holds an image that's showcased on the frame and
 * a timestamp of that frame in the video.
 * 
 * @param frame the image showcased on the frame
 * @param timeStamp the time when the frame is shown
 */
public record VideoFrame(BufferedImage frame, double timeStamp) {
}
