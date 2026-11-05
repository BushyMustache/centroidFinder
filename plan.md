# Overall Goal
The overall goal of this project will be to make your centroid finder project be able to process mp4 videos. When working properly, it will generate a CSV that tracks the largest centroid over time. The first column will be the number of seconds since the beginning of the video, and the second and third columns will be the x and y coordinates of the largest found centroid at that time. If no centroid is found at that timestamp, coordinates of (-1, -1) should be used. In this portion of the project you will be taking each frame of video and feeding it to your centroid finder logic. You will create a command line utility that can process videos, for real!

You will be creating a jar that can run like so:

java -jar videoprocessor.jar inputPath outputCsv targetColor threshold
Everything after videoprocessor.jar is command line arguments that will be taken in.

## General Plan
- Get each frame from the video
- Use each frame as an image and binarize each image group - BinarizingImageGroupFinder
- Get the largest centroid based on the largest group of each frame
- Count the frames that have passed to get the time passed
- Output the time, and the x and y coordinates of the largest centroid

## ImageSummaryApp
- This is where we will make the most changes. Currently, this class works for images, but not for videos

## DfsBinaryGroupFinder
- Make a new class that implements the DfsBinaryGroupFinder class
- The new class will get the largest group that was output rather than all groups

## New Classes or Implementations
- Make a application entry point for our video processor (similar to ImageSummaryApp)
- Make a class to read video frames and timestamps
- Make a class for coordinate frame processing
- Make a class for writing the largest centroid and (x, y) coordinates to a CSV file
- Make tests for each class

## Salamander Video Test
- We tested the salamander video by getting
- Color - through an online color picker and selecting the salamander
- Threshold - we ran the program multiple times at different thresholds and found ~10-20 works well
- One issue was that part of the table has a similar color to the salamandar, causing the program to track that part of the table when the salamander was mostly off screen
- To test if the tracker was working, we manually got the length and width of random frames and compared it to the csv output