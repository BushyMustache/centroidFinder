package io.github.BushyMustache.centroidFinder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesHeaderWhenResultsAreEmpty() throws IOException {
        Path output = tempDir.resolve("results.csv");

        CsvWriter writer = new CsvWriter();
        writer.write(output, List.of());

        List<String> lines = Files.readAllLines(output);

        assertEquals(List.of("timestamp,x,y"), lines);
    }

    @Test
    void writesOneFrameAnalysis() throws IOException {
        Path output = tempDir.resolve("results.csv");

        List<VideoProcessor.FrameAnalysis> results = List.of(new VideoProcessor.FrameAnalysis(0.033, 120, 85));

        CsvWriter writer = new CsvWriter();
        writer.write(output, results);

        List<String> lines = Files.readAllLines(output);

        assertEquals(2, lines.size());
        assertEquals("timestamp,x,y", lines.get(0));
        assertEquals("0.033,120,85", lines.get(1));
    }

    @Test
    void writesMinusOneCoordinatesWhenNoCentroidIsFound() throws IOException {
        Path output = tempDir.resolve("results.csv");

        List<VideoProcessor.FrameAnalysis> results = List.of(new VideoProcessor.FrameAnalysis(0.067, -1, -1));

        CsvWriter writer = new CsvWriter();
        writer.write(output, results);

        List<String> lines = Files.readAllLines(output);

        assertEquals("0.067,-1,-1", lines.get(1));
    }

    @Test
    void writesMultipleResultsInOrder() throws IOException {
        Path output = tempDir.resolve("results.csv");

        List<VideoProcessor.FrameAnalysis> results = List.of(
            new VideoProcessor.FrameAnalysis(0.0, 10, 20),
            new VideoProcessor.FrameAnalysis(0.033, 30, 40),
            new VideoProcessor.FrameAnalysis(0.067, -1, -1)
        );

        CsvWriter writer = new CsvWriter();
        writer.write(output, results);

        List<String> lines = Files.readAllLines(output);

        assertEquals(List.of("timestamp,x,y", "0.0,10,20", "0.033,30,40", "0.067,-1,-1"), lines);
    }
}

