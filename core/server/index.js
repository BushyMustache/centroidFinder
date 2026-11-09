const express = require('express');
const videoRoutes = require('./routes/videoRoutes');

const app = express();
app.use('/api', videoRoutes);
const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

