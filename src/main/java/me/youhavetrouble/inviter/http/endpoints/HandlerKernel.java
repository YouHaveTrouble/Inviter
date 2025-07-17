package me.youhavetrouble.inviter.http.endpoints;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.youhavetrouble.inviter.Main;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class HandlerKernel implements HttpHandler {

    private final Set<EndpointHandler> handlers = new HashSet<>();

    public HandlerKernel() {
        handlers.add(new GetDiscordInviteByGuildId());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        for (EndpointHandler handler : handlers) {
            if (handler.pathPattern().matcher(path).matches()) {
                handler.handle(exchange);
                return;
            }
        }

        try {
            exchange.sendResponseHeaders(404, -1);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            Main.LOGGER.error("Error handling request for {}: {}", path, e.getMessage());
        }

    }
}
