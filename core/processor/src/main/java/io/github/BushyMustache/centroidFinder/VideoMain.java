package io.github.BushyMustache.centroidFinder;

import java.util.List;
import java.nio.file.Path;

/**
 * The main application for analyzing a video.
 * After inputting a correct argument, the app will read the video,
 * calculate the centroid coordinates and write them to a CSV file named
 * output.csv that will either be updated or created in the sampleOutput folder.
 * 
 * Make sure to add these four things in order in the arguments.
 * <videoPath> <outputDirectory> <targetColor> <threshold>
 * 
 * Example: "src/sampleInput/video.mp4 sampleOutput #FFFFFF 10"
 */
public class VideoMain {
    public static void main(String[] args) {

        try {
            // Read the arguments passed by Express.
            if (args.length != 4) {
                throw new IllegalArgumentException(
                    "Expected: <videoPath> <outputDirectory> <targetColor> <threshold>"
                );
            }

            String videoPath = args[0];
            Path outputDirectory = Path.of(args[1]);
            int targetColor = Integer.parseInt(args[2], 16);
            int threshold = Integer.parseInt(args[3]);

            ColorDistanceFinder distanceFinder =
                new EuclideanColorDistance();

            ImageBinarizer binarizer =
                new DistanceImageBinarizer(
                    distanceFinder,
                    targetColor,
                    threshold
                );

            ImageGroupFinder groupFinder =
                new BinarizingImageGroupFinder(
                    binarizer,
                    new DfsBinaryGroupFinder()
                );

            VideoProcessor processor = new VideoProcessor(groupFinder);

            List<FrameAnalysis> results =
                processor.processVideo(videoPath);

            CsvWriter csvWriter = new CsvWriter();

            csvWriter.write(
                outputDirectory.resolve("output.csv"),
                results
            );

            System.out.println("Video processing completed.");
        } catch (Exception e) {
            System.err.println("Error processing video:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}