package me.youhavetrouble.inviter.http.endpoints;

import com.sun.net.httpserver.HttpExchange;
import me.youhavetrouble.inviter.Main;
import me.youhavetrouble.inviter.discord.DiscordInvite;
import me.youhavetrouble.inviter.discord.DiscordInviteManager;
import me.youhavetrouble.inviter.discord.GuildSettings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.regex.Pattern;

public class MainEndpoint implements EndpointHandler {

    private final Pattern pathPattern = Pattern.compile("^/$");

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

        String host = exchange.getRemoteAddress().getHostName();

        GuildSettings guildSettings = Main.getStorage().getGuildSettings(host);
        if (guildSettings == null) {
            exchange.sendResponseHeaders(404, -1); // Not Found
            return;
        }

        DiscordInviteManager inviteManager = Main.getDiscordInviteMenager();
        DiscordInvite invite = inviteManager.getInvite(guildSettings.guildId());

        if (invite == null) {
            exchange.sendResponseHeaders(404, -1); // Not Found
            return;
        }

        exchange.getResponseHeaders().set("Location", "https://discord.gg/" + invite.code());
        exchange.sendResponseHeaders(307, -1);
    }
}
