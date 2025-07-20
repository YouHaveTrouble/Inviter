package me.youhavetrouble.inviter.http.endpoints;

import com.sun.net.httpserver.HttpExchange;
import me.youhavetrouble.inviter.Main;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MainEndpoint implements EndpointHandler {

    private final Pattern pathPattern = Pattern.compile("^/$");

    private final String template;

    public MainEndpoint() {
        String rawTemplate = null;
        try (InputStream resource = this.getClass().getResourceAsStream("/template/index.html")) {
            rawTemplate = new String(resource.readAllBytes());
            rawTemplate = rawTemplate.replaceAll("\\{\\{discord_app_id}}", Main.getJda().getSelfUser().getApplicationId());
            rawTemplate = rawTemplate.replaceAll("\\{\\{base_url}}", Main.baseUrl);
        } catch (IOException | NullPointerException e) {
            Main.LOGGER.warn("Failed to load template for main endpoint", e);
        }
        this.template = rawTemplate;
    }

    @NotNull
    @Override
    public Pattern pathPattern() {
        return pathPattern;
    }

    @Override
    public void handle(@NotNull HttpExchange exchange) throws IOException {
        if (template == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, template.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(template.getBytes(StandardCharsets.UTF_8));
        exchange.getResponseBody().close();

    }

}
