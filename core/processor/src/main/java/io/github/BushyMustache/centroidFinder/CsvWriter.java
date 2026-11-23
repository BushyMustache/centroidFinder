package io.github.BushyMustache.centroidFinder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvWriter {
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
