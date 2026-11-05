package io.github.BushyMustache.centroidFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VideoProcessor {

    private final ImageGroupFinder groupFinder;

    public VideoProcessor(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

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

    public record FrameAnalysis(double timestamp, int x, int y) {
    }
}
