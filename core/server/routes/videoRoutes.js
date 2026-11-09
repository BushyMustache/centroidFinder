const express = require('express');

const router = express.Router();

router.get('/videos', (req, res) => {
    res.json([]);
});

export default router;