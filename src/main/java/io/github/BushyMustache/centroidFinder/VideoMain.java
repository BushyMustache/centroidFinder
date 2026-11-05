package io.github.BushyMustache.centroidFinder;

import java.util.List;
import java.nio.file.Path;

public class VideoMain{
    public static void main(String[] args){
        ColorDistanceFinder distanceFinder =
            new EuclideanColorDistance();

        ImageBinarizer binarizer =
            new DistanceImageBinarizer(
                distanceFinder,
                0x55000a,
                20
            );

        ImageGroupFinder groupFinder =
            new BinarizingImageGroupFinder(
                binarizer,
                new DfsBinaryGroupFinder()
            );

        VideoProcessor processor = new VideoProcessor(groupFinder);
        try {
            List<VideoProcessor.FrameAnalysis> results = processor.processVideo("sampleInput/ensantina.mp4");

            CsvWriter csvWriter = new CsvWriter();
            
            csvWriter.write(
                Path.of("sampleOutput", "output.csv"),
                results
            );

            for (VideoProcessor.FrameAnalysis result : results) {
                System.out.println(result);
            }
        } catch (Exception e) {
            System.err.println("Error processing video: " + e.getMessage());
        }

    }
}