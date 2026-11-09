const express = require('express');
const fs = require('fs');
const path = require('path');

const router = express.Router();

const VIDEO_DIR = path.resolve(__dirname, '../../processor/sampleInput');

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

module.exports = router;