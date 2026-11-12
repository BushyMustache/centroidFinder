const express = require('express');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const crypto = require('crypto');

console.log('FFMPEG_PATH:', process.env.FFMPEG_PATH);

require('dotenv').config({
    path: path.resolve(__dirname, '../../../.env')
});

const router = express.Router();

const VIDEO_DIR = path.resolve(__dirname, '../../processor/sampleInput');

const OUTPUT_DIR = path.resolve(
    __dirname,
    '../../processor/sampleOutput'
);

const JAVA_PATH = process.env.JAVA_PATH || 'java';
const PROCESSOR_JAR = path.resolve(
    __dirname,
    '../../processor/target/centroidFinder-1.0-SNAPSHOT-jar-with-dependencies.jar'
);

const jobs = new Map();

const FFMPEG_PATH = process.env.FFMPEG_PATH;

router.get('/videos', (req, res) => {
    fs.readdir(VIDEO_DIR, (err, files) => {
        if (err) {
            console.error("Error reading video directory:", err);
            return res.status(500).json({
                error: "Error reading video directory"
            });
        }

        const videos = files.filter(file => 
            /\.(mp4|mov|avi|mkv|webm)$/i.test(file)
        );

        res.json(videos);
    });
});

router.get('/thumbnail/:filename', (req, res) => {
    const filename = req.params.filename;

    // Validate filename
    if (
        path.basename(filename) !== filename ||
        !/\.(mp4|mov|avi|mkv|webm)$/i.test(filename)
    ) {
        return res.status(400).json({
            error: 'Invalid video filename'
        });
    }

    const videoPath = path.join(VIDEO_DIR, filename);

    // Check whether video exists
    if (!fs.existsSync(videoPath)) {
        return res.status(404).json({
            error: 'Video not found'
        });
    }

    const ffmpeg = spawn(FFMPEG_PATH, [
        '-hide_banner',
        '-loglevel', 'error',
        '-i', videoPath,
        '-frames:v', '1',
        '-f', 'image2pipe',
        '-vcodec', 'mjpeg',
        'pipe:1'
    ]);

    const imageChunks = [];
    let errorOutput = '';

    ffmpeg.stdout.on('data', chunk => {
        imageChunks.push(chunk);
    });

    ffmpeg.stderr.on('data', chunk => {
        errorOutput += chunk.toString();
    });

    ffmpeg.on('error', err => {
        console.error('FFmpeg execution error:', err);

        if (!res.headersSent) {
            res.status(500).json({
                error: 'Unable to execute FFmpeg'
            });
        }
    });

    ffmpeg.on('close', code => {
        if (res.headersSent) {
            return;
        }

        if (code !== 0) {
            console.error('FFmpeg failed:', errorOutput);

            return res.status(500).json({
                error: 'Unable to generate thumbnail'
            });
        }

        const imageBuffer = Buffer.concat(imageChunks);

        if (imageBuffer.length === 0) {
            return res.status(500).json({
                error: 'No thumbnail was generated'
            });
        }

        res.type('image/jpeg').send(imageBuffer);
    });
});

router.post('/process/:filename', (req, res) => {
    const { filename } = req.params;
    const { targetColor, threshold } = req.query;

    // Validate required query parameters.
    if (targetColor === undefined || threshold === undefined) {
        return res.status(400).json({
            error: 'Missing targetColor or threshold query parameter.'
        });
    }

    // Validate the video filename to prevent directory traversal.
    if (
        path.basename(filename) !== filename ||
        !/\.(mp4|mov|avi|mkv|webm)$/i.test(filename)
    ) {
        return res.status(400).json({
            error: 'Invalid video filename'
        });
    }

    // Accept either ff0000 or #ff0000.
    const normalizedColor = targetColor.replace(/^#/, '');

    if (!/^[0-9a-fA-F]{6}$/.test(normalizedColor)) {
        return res.status(400).json({
            error: 'Invalid targetColor. Expected a six-digit hex color.'
        });
    }

    // Convert the query string to a number and validate it.
    const numericThreshold = Number(threshold);

    if (
        !Number.isSafeInteger(numericThreshold) ||
        numericThreshold < 0
    ) {
        return res.status(400).json({
            error: 'Invalid threshold. Expected a non-negative integer.'
        });
    }

    const videoPath = path.join(VIDEO_DIR, filename);

    if (!fs.existsSync(videoPath)) {
        return res.status(404).json({
            error: 'Video not found'
        });
    }

    // Check that the Java processor has been built.
    if (!fs.existsSync(PROCESSOR_JAR)) {
        console.error('Processor JAR not found:', PROCESSOR_JAR);

        return res.status(500).json({
            error: 'Error starting job'
        });
    }

    // Generate a unique ID and output directory for this job.
    const jobId = crypto.randomUUID();
    const jobOutputDir = path.join(OUTPUT_DIR, jobId);

    try {
        fs.mkdirSync(jobOutputDir, { recursive: true });

        const javaProcess = spawn(JAVA_PATH, [
            '-jar',
            PROCESSOR_JAR,
            videoPath,
            jobOutputDir,
            normalizedColor,
            String(numericThreshold)
        ]);

        // Save the job so its status can be checked later.
        jobs.set(jobId, {
            status: 'processing',
            video: filename,
            outputDirectory: jobOutputDir,
            startedAt: new Date().toISOString()
        });

        let errorOutput = '';

        javaProcess.stderr.on('data', chunk => {
            errorOutput += chunk.toString();
        });

        javaProcess.on('error', err => {
            console.error(`Unable to start job ${jobId}:`, err);

            jobs.set(jobId, {
                ...jobs.get(jobId),
                status: 'failed',
                error: err.message
            });
        });

        javaProcess.on('close', code => {
            if (code === 0) {
                jobs.set(jobId, {
                    ...jobs.get(jobId),
                    status: 'completed',
                    completedAt: new Date().toISOString()
                });
            } else {
                console.error(`Job ${jobId} failed:`, errorOutput);

                jobs.set(jobId, {
                    ...jobs.get(jobId),
                    status: 'failed',
                    error: errorOutput || `Java process exited with code ${code}`
                });
            }
        });

        // Return immediately; Java continues processing in the background.
        return res.status(202).json({ jobId });

    } catch (err) {
        console.error('Error starting job:', err);

        return res.status(500).json({
            error: 'Error starting job'
        });
    }
});

module.exports = router;