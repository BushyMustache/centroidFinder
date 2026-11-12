const express = require('express');
const path = require('path');

require('dotenv').config({
    path: path.resolve(__dirname, '../../.env')
});

const videoRoutes = require('./routes/videoRoutes');

const app = express();
const VIDEO_DIR = path.resolve(__dirname, '../processor/sampleInput');

app.use('/videos', express.static(VIDEO_DIR));
app.use('/api', videoRoutes);

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Server running on port http://localhost:${PORT}`);
});

