# PO:RT - Personal Overworld: Realm Transfer

Paper plugin + web shop for personal worlds and ticketed visits.

## Features
- Personal worlds per player (pworld_<uuid>) created on first join.
- Commands: /home(non-op can use this), /leave (non-op can use this), /join <username> (op only).
- Auto-teleport to personal world spawn on join.
- Shared Nether/End; portals route to shared spawn and return to last overworld location.
- Auto-unload empty personal worlds after 30 seconds.
- Ticket system: invitation ticket opens a GUI to issue entrance tickets to other players' worlds.
- Local HTTP API to buy tickets using diamonds.

## Requirements
- Java 21
- Paper 1.21 server
- Node.js (for the web app)

## Project layout
- src/ - Paper plugin source
- Web/ - Express server + API proxy
- Web/front/ - Vite React frontend

## Build the plugin
1) Build the jar:

```powershell
.\gradlew jar
```

2) The jar output is configured in `build.gradle`:
- Name: `port.jar`
- Destination: `C:\Users\endiy\Desktop\ToyoTech\Plugin`

3) Copy the jar into your Paper server `plugins/` folder (or change the destination path).

## Run the plugin
1) Start the Paper server once to generate `plugins/PORT/config.yml`.
2) Update config as needed (see Config section).
3) Restart the server.

## Build and run the web app
1) Build the frontend:

```powershell
cd Web\front
npm install
npm run build
```

2) Run the Express server:

```powershell
cd ..
npm install
node server.js
```

3) Open:
- http://localhost:3000/create
- http://localhost:3000/shop/<userId>

Note: `/` returns 204 by design. The Express server serves `Web/front/dist`.

## Config
### Plugin (Paper)
`src/main/resources/config.yml` (copied to `plugins/PORT/config.yml` on first run):
- `mainWorld`: main overworld name (default: `world`)
- `webPort`: HTTP API port (default: `8765`)
- `secret`: shared secret for the HTTP API

### Web server
`Web/config.json`:
- `webPort`: Express port (default: `3000`)
- `pluginApiUrl`: Paper plugin HTTP API base URL (default: `http://127.0.0.1:8765`)
- `pluginSecret`: must match the plugin `secret`

Important: the plugin HTTP server binds to `127.0.0.1`, so the web server must run on the same machine unless you change the bind address in code.

## HTTP API
### Plugin API (Paper)
`POST /api/tickets/buy`
- Header: `X-Plugin-Secret: <secret>`
- Body: `{ "playerName": "Steve" }`
- Response:
  - `{ "ok": true }`
  - `{ "ok": false, "error": "PLAYER_OFFLINE" }`
  - `{ "ok": false, "error": "INSUFFICIENT_DIAMONDS" }`

### Web API (Express)
`GET /api/shop/resolve/:userId`
- Response: `{ "ok": true, "username": "Steve" }` or `{ "ok": false, "error": "INVALID_USERID" }`

`POST /api/shop/buy`
- Body: `{ "userId": "<base64url>" }`
- Proxies to the plugin API.

## License
Apache License 2.0. See `LICENSE`.