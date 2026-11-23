package io.github.BushyMustache.centroidFinder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The class responsible for writing the frame data to a CSV file.
 * Not responsible for creating a CSV file before writing to it.
 */
public class CsvWriter {
    /**
     * Writes down the timestamp and centroid coordinates for each
     * piece of data. The length this task can take depends on the video
     * that's processed.
     * 
     * @param outputPath the path to the CSV file to write on
     * @param results the data that will be written
     * @throws IOException for any I/O errors
     */
    public void write(Path outputPath, List<FrameAnalysis> results) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("timestamp,x,y");
            writer.newLine();

            for (FrameAnalysis result : results) {
                writer.write(
                    result.timestamp() + ","
                    + result.x() + ","
                    + result.y()
                );
                
                writer.newLine();
            }
        }
    }
}
