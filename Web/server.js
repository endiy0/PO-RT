const express = require('express');
const axios = require('axios');
const fs = require('fs');
const path = require('path');
const config = require('./config.json');

const app = express();
app.use(express.json());

// 1. Root must be empty
app.get('/', (req, res) => {
    res.status(204).send();
});

// 2. Static files (JS, CSS) - but NOT index.html on root
// serve 'assets' or everything in dist, but strict routing for /
app.use(express.static(path.join(__dirname, 'front/dist'), { index: false }));

// 3. API
const decodeUserId = (uid) => {
    try {
        return Buffer.from(uid, 'base64url').toString('utf8');
    } catch (e) {
        return null;
    }
};

app.get('/api/shop/resolve/:userId', (req, res) => {
    const username = decodeUserId(req.params.userId);
    if (!username) {
        return res.json({ ok: false, error: "INVALID_USERID" });
    }
    res.json({ ok: true, username });
});

app.post('/api/shop/buy', async (req, res) => {
    const { userId } = req.body;
    if (!userId) return res.status(400).json({ ok: false, error: "Missing userId" });

    const username = decodeUserId(userId);
    if (!username) return res.status(400).json({ ok: false, error: "Invalid userId" });

    try {
        const pluginResponse = await axios.post(`${config.pluginApiUrl}/api/tickets/buy`, {
            playerName: username
        }, {
            headers: {
                'X-Plugin-Secret': config.pluginSecret
            }
        });
        res.json(pluginResponse.data);
    } catch (err) {
        if (err.response) {
            res.status(err.response.status).json(err.response.data);
        } else {
            console.error("Plugin API Error:", err.message);
            res.status(500).json({ ok: false, error: "PLUGIN_UNAVAILABLE" });
        }
    }
});

// 4. SPA Routing (Create & Shop)
const serveIndex = (req, res) => {
    res.sendFile(path.join(__dirname, 'front/dist/index.html'));
};

app.get('/create', serveIndex);
app.get('/shop/*', serveIndex);

app.listen(config.webPort, () => {
    console.log(`Web server running on port ${config.webPort}`);
});
