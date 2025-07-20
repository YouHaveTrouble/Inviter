package me.youhavetrouble.inviter.http.endpoints;

import com.sun.net.httpserver.HttpExchange;
import me.youhavetrouble.inviter.discord.DiscordInvite;
import me.youhavetrouble.inviter.Main;
import me.youhavetrouble.inviter.discord.DiscordInviteManager;
import me.youhavetrouble.inviter.discord.GuildSettings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class GetDiscordInviteByGuildId implements EndpointHandler {

    /**
     * Technically shortest dsicord snowflake is 7 characters and current longest is 19, so kinda-futureproofing it and going with 20.
     */
    private final Pattern pathPattern = Pattern.compile("^/invite/\\d{7,20}$");

    private final String invitesDisabledTemplate, botNotInGuildTemplate;

    public GetDiscordInviteByGuildId() {
        String invitesDisabledTemplate = null;
        try (InputStream resource = this.getClass().getResourceAsStream("/template/invites-paused.html")) {
            assert resource != null;
            invitesDisabledTemplate = new String(resource.readAllBytes());
            invitesDisabledTemplate = invitesDisabledTemplate.replaceAll("\\{\\{base_url}}", Main.baseUrl);
        } catch (IOException | NullPointerException e) {
            Main.LOGGER.warn("Failed to load template for invites disabled page", e);
        }
        this.invitesDisabledTemplate = invitesDisabledTemplate;

        String botNotInGuildTemplate = null;
        try (InputStream resource = this.getClass().getResourceAsStream("/template/guild-not-supported.html")) {
            assert resource != null;
            botNotInGuildTemplate = new String(resource.readAllBytes());
            botNotInGuildTemplate = botNotInGuildTemplate.replaceAll("\\{\\{base_url}}", Main.baseUrl);
        } catch (IOException | NullPointerException e) {
            Main.LOGGER.warn("Failed to load template for guild not supported page", e);
        }
        this.botNotInGuildTemplate = botNotInGuildTemplate;
    }

    @NotNull
    @Override
    public Pattern pathPattern() {
        return pathPattern;
    }

    @Override
    public void handle(@NotNull HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String path = exchange.getRequestURI().getPath();

        String[] parts = path.split("/");
        String guildId = parts[parts.length - 1];
        long guildIdLong;
        try {
            guildIdLong = Long.parseLong(guildId);
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        GuildSettings settings = Main.getStorage().getGuildSettings(guildIdLong);

        if (!settings.invitesEnabled()) {
            sendInvitesPausedTemplate(exchange);
            return;
        }

        DiscordInviteManager inviteManager = Main.getDiscordInviteMenager();
        DiscordInvite invite = inviteManager.getInvite(guildIdLong);

        if (invite == null) {
            sendBotNotInGuildTemplate(exchange);
            return;
        }

        String inviteUrl = "https://discord.gg/" + invite.code();

        switch (exchange.getRequestHeaders().getFirst("Accept")) {
            case "text/plain" -> {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, inviteUrl.length());
                exchange.getResponseBody().write(inviteUrl.getBytes());
            }
            case "application/json" -> {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                String jsonResponse = "{\"url\": \"" + inviteUrl + "\"}";
                exchange.sendResponseHeaders(200, jsonResponse.length());
                exchange.getResponseBody().write(jsonResponse.getBytes());
            }
            default -> {
                exchange.getResponseHeaders().set("Location", inviteUrl);
                exchange.sendResponseHeaders(307, -1);
            }
        }
    }


    private void sendInvitesPausedTemplate(HttpExchange exchange) throws IOException {
        String message = "Guild you were invited to currently has invites disabled. Try again later.";
        switch (exchange.getRequestHeaders().getFirst("Accept")) {
            case "text/plain" -> {
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

                exchange.sendResponseHeaders(423, message.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
            }
            case "application/json" -> {
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                String jsonResponse = "{\"error\": \"%s\"}".formatted(message);
                exchange.sendResponseHeaders(423, jsonResponse.length());
                exchange.getResponseBody().write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
            default -> {
                if (invitesDisabledTemplate != null) {
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(423, invitesDisabledTemplate.getBytes(StandardCharsets.UTF_8).length);
                    exchange.getResponseBody().write(invitesDisabledTemplate.getBytes(StandardCharsets.UTF_8));
                    exchange.getResponseBody().close();
                } else {
                    exchange.sendResponseHeaders(423, -1);
                }
            }
        }
    }

    private void sendBotNotInGuildTemplate(HttpExchange exchange) throws IOException {
        String message = "Guild you were invited to is not supported by the bot. Try again later.";
        switch (exchange.getRequestHeaders().getFirst("Accept")) {
            case "text/plain" -> {
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(404, message.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
            }
            case "application/json" -> {
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                String jsonResponse = "{\"error\": \"%s\"}".formatted(message);
                exchange.sendResponseHeaders(404, jsonResponse.length());
                exchange.getResponseBody().write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
            default -> {
                if (botNotInGuildTemplate != null) {
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(404, botNotInGuildTemplate.getBytes(StandardCharsets.UTF_8).length);
                    exchange.getResponseBody().write(botNotInGuildTemplate.getBytes(StandardCharsets.UTF_8));
                    exchange.getResponseBody().close();
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
        }
    }

}
