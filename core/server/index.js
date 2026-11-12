const express = require('express');
const videoRoutes = require('./routes/videoRoutes');
const path = require('path');

const app = express();
const VIDEO_DIR = path.resolve(__dirname, '../processor/sampleInput');

app.use('/videos', express.static(VIDEO_DIR));
app.use('/api', videoRoutes);

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Server running on port http://localhost:${PORT}`);
});

