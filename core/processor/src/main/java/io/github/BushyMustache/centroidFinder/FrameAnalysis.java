package io.github.BushyMustache.centroidFinder;

/**
 * Holds the timestamp and centroid coordinates of a specific frame
 * in a video file.
 * 
 * @param timestamp a specific timestamp of a video
 * @param x the x coordinate of the centroid
 * @param y the y coordinate of the centroid
 */
public record FrameAnalysis(double timestamp, int x, int y) {
}
