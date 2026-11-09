# Salamander API Notes:

## What we Understand
- We will GET the videos from the api, returning the videos
- We will have to generate a jpg thumbnail based on the first frame of an available video by GETting the filename of the video
- By using query parameters, we'll take in the file name, target color, and threshold, then generate a unique job ID using UUID. We'll then start the jar as a process and return the job ID to the client   
- We will need to GET the job status of a specific jobId, indicating that that specific video is processing or completed, otherwise showing an error or not found

## Questions
- How should we get express to communicate with Java?
- How do we store outputs and videos?

# Server Plan
We will divide the server into five responsibilities
- API routing - Receives and responds to HTTP requests
- Video management - Finds available videos and generates thumbnails
- Job management - Creates job IDs and tracks processing status
- Java process management - Launches and monitors your Java JAR
- File management - Stores videos, CSV results, and job information