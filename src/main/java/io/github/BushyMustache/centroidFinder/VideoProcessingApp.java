package io.github.BushyMustache.centroidFinder;

import java.util.List;

public class VideoProcessingApp{
    public static void main(String[] args){
        ColorDistanceFinder distanceFinder =
            new EuclideanColorDistance();

        ImageBinarizer binarizer =
            new DistanceImageBinarizer(
                distanceFinder,
                0xEB8F16,
                10
            );

        ImageGroupFinder groupFinder =
            new BinarizingImageGroupFinder(
                binarizer,
                new DfsBinaryGroupFinder()
            );

        VideoProcessor processor = new VideoProcessor(groupFinder);
        try {
            List<VideoProcessor.FrameAnalysis> results = processor.processVideo("sampleInput/moving-square-triangle.mp4");

            for (VideoProcessor.FrameAnalysis result : results) {
                System.out.println(result);
            }
        } catch (Exception e) {
            System.err.println("Error processing video: " + e.getMessage());
        }

    }
}