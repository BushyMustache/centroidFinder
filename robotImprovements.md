# Improvements

## Optimization
- Avoid decoding and converting frames you won’t analyze.
  - `VideoProcessor.java` analyzes at most one frame per second, but `VideoReader.java` still decodes and converts every frame. Consider configurable sampling rates and seeking to the desired timestamps. Seeking can affect timestamp accuracy, so test it against the current output.
- Reduce work in connected-component finding.
  - `VideoProcessor.java` uses only the largest group, while `DfsBinaryGroupFinder.java` finds and sorts all groups. An iterative traversal (rather than recursive DFS) can also avoid stack-overflow risk and repeated per-pixel allocations.

## Other Improvements
- Control server job load. 
  - `videoRoutes.js` starts a separate Java process for each job and keeps job state in memory. Add a concurrency limit or queue before allowing many simultaneous jobs.