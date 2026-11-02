
package io.github.BushyMustache.centroidFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VideoProcessor{

    private final ImageGroupFinder groupFinder;

    public VideoProcessor(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    public List<FrameAnalysis> processVideo(String videoPath) throws Exception {
        List<FrameAnalysis> results = new ArrayList<>();
        int x = -1;        
        int y = -1;
        
        try (VideoReader reader = new VideoReader(videoPath)) {
            Optional<VideoFrame> nextFrame;

            while ((nextFrame = reader.readNextFrame()).isPresent()) {
                VideoFrame videoFrame = nextFrame.get();
                List<Group> groups = groupFinder.findConnectedGroups(videoFrame.frame());

                if (!groups.isEmpty()) {
                    Group largestGroup = groups.get(0);
                    x = largestGroup.centroid().x();
                    y = largestGroup.centroid().y();
                }
                results.add(new FrameAnalysis(videoFrame.timeStamp(), x, y));
            }
        }
        return results;
    }

    public record FrameAnalysis(double timestamp, int x, int y) {
    }
}
