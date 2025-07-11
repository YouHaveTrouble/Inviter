package me.youhavetrouble.inviter.http.endpoints;

import com.sun.net.httpserver.HttpExchange;
import me.youhavetrouble.inviter.discord.DiscordInvite;
import me.youhavetrouble.inviter.Main;
import me.youhavetrouble.inviter.discord.DiscordInviteManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.regex.Pattern;

public class GetDiscordInviteByGuildId implements EndpointHandler {

    private final Pattern pathPattern = Pattern.compile("^/api/v1/discord/\\d{10,18}$");

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

        DiscordInviteManager inviteManager = Main.getDiscordInviteMenager();
        DiscordInvite invite = inviteManager.getInvite(guildIdLong);

        if (invite == null) {
            exchange.sendResponseHeaders(404, -1); // Not Found
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


}
