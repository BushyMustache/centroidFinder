const express = require('express');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

console.log('FFMPEG_PATH:', process.env.FFMPEG_PATH);

require('dotenv').config({
    path: path.resolve(__dirname, '../../../.env')
});

const router = express.Router();

const VIDEO_DIR = path.resolve(__dirname, '../../processor/sampleInput');

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

module.exports = router;