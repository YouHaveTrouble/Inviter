package me.youhavetrouble.inviter.http.endpoints;

import com.sun.net.httpserver.HttpExchange;
import me.youhavetrouble.inviter.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class IconEndpoint implements EndpointHandler {

    private final Pattern pathPattern = Pattern.compile("^/icon$");

    private final byte[] imageContent;

    public IconEndpoint() {
        byte[] rawTemplate = null;
        try (InputStream resource = this.getClass().getResourceAsStream("/asset/icon.png")) {
            rawTemplate = resource.readAllBytes();
        } catch (IOException | NullPointerException e) {
            Main.LOGGER.warn("Failed to load template for main endpoint", e);
        }
        this.imageContent = rawTemplate;
    }

    @NotNull
    @Override
    public Pattern pathPattern() {
        return pathPattern;
    }

    @Override
    public void handle(@NotNull HttpExchange exchange) throws IOException {
        if (imageContent == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.sendResponseHeaders(200, imageContent.length);
        exchange.getResponseBody().write(imageContent);
        exchange.getResponseBody().close();
    }

}
