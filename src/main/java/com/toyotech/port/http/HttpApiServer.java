package com.toyotech.port.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.toyotech.port.PORTPlugin;
import com.toyotech.port.data.DataManager;
import com.toyotech.port.items.TicketListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class HttpApiServer {
    private final PORTPlugin plugin;
    private HttpServer server;
    private final Gson gson = new Gson();

    public HttpApiServer(PORTPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int port = plugin.getConfig().getInt("webPort", 8765);
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
            server.createContext("/api/tickets/buy", new BuyHandler());
            server.setExecutor(null); // default executor
            server.start();
            plugin.getLogger().info("HTTP Server started on 127.0.0.1:" + port);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start HTTP server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class BuyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, error("Method Not Allowed"));
                return;
            }

            // Verify Secret
            String secret = exchange.getRequestHeaders().getFirst("X-Plugin-Secret");
            String expected = plugin.getConfig().getString("secret");
            if (expected != null && !expected.equals(secret)) {
                sendResponse(exchange, 403, error("Forbidden"));
                return;
            }

            // Parse Body
            JsonObject body;
            try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                body = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                sendResponse(exchange, 400, error("Invalid JSON"));
                return;
            }

            String playerName = body.has("playerName") ? body.get("playerName").getAsString() : null;
            if (playerName == null) {
                sendResponse(exchange, 400, error("Missing playerName"));
                return;
            }

            // Execute logic on Main Thread
            try {
                JsonObject result = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    Player p = Bukkit.getPlayerExact(playerName);
                    if (p == null) {
                        return error("PLAYER_OFFLINE");
                    }
                    
                    if (!p.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 1)) {
                        return error("INSUFFICIENT_DIAMONDS");
                    }

                    p.getInventory().removeItem(new ItemStack(Material.DIAMOND, 1));
                    
                    // Give Ticket Item
                    p.getInventory().addItem(TicketListener.getInvitationTicket());

                    JsonObject ok = new JsonObject();
                    ok.addProperty("ok", true);
                    return ok;
                }).get();

                int code = result.get("ok").getAsBoolean() ? 200 : 400;
                sendResponse(exchange, code, result);

            } catch (InterruptedException | ExecutionException e) {
                plugin.getLogger().severe("Error processing buy ticket: " + e.getMessage());
                sendResponse(exchange, 500, error("Internal Server Error"));
            }
        }
    }

    private JsonObject error(String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("ok", false);
        json.addProperty("error", msg);
        return json;
    }

    private void sendResponse(HttpExchange exchange, int code, JsonObject json) throws IOException {
        String resp = gson.toJson(json);
        byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
