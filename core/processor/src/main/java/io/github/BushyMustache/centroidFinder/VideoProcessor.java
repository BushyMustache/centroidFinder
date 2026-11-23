package io.github.BushyMustache.centroidFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The class that calculates the centroid coordinates of the frame for each second in
 * the video. Utilizes methods in VideoReader (check out VideoReader documentation for more clarity)
 */
public class VideoProcessor {

    private final ImageGroupFinder groupFinder;

    /**
     * The constructor for the VideoProcessor
     * @param groupFinder finds connected groups of pixels in a image frame
     */
    public VideoProcessor(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    /**
     * Processes the video and calculates the centroid coordinates and adds
     * them to a list with a timestamp included. Returns the list after
     * processing is finished.
     * 
     * @param videoPath the path to the video file
     * @return a list with each piece of data containing a timestamp and the centroid coordinates
     * @throws Exception if any kind of error occurs
     */
    public List<FrameAnalysis> processVideo(String videoPath) throws Exception {
        List<FrameAnalysis> results = new ArrayList<>();

        long lastProcessedSecond = -1;

        try (VideoReader reader = new VideoReader(videoPath)) {
            Optional<VideoFrame> nextFrame;

            while ((nextFrame = reader.readNextFrame()).isPresent()) {
                VideoFrame videoFrame = nextFrame.get();

                long currentSecond = (long) videoFrame.timeStamp();

                // Skip frames if we have already processed this second
                if (currentSecond == lastProcessedSecond) {
                    continue;
                }

                lastProcessedSecond = currentSecond;

                int x = -1;
                int y = -1;

                List<Group> groups =
                    groupFinder.findConnectedGroups(videoFrame.frame());

                if (!groups.isEmpty()) {
                    Group largestGroup = groups.get(0);
                    x = largestGroup.centroid().x();
                    y = largestGroup.centroid().y();
                }

                results.add(
                    new FrameAnalysis(videoFrame.timeStamp(), x, y)
                );
            }
        }

        return results;
    }
}
