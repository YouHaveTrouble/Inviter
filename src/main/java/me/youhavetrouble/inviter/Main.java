package me.youhavetrouble.inviter;

import me.youhavetrouble.inviter.http.ApiServer;
import me.youhavetrouble.inviter.discord.DiscordInviteManager;
import me.youhavetrouble.inviter.storage.SqliteStorage;
import me.youhavetrouble.inviter.storage.Storage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger("Inviter");

    private static JDA jda;
    private static DiscordInviteManager discordInviteManager;
    private static ApiServer apiServer;
    private static Storage storage;

    public static void main(String[] args) throws InterruptedException {

        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isEmpty()) {
            LOGGER.error("Discord token is not set. Please set the DISCORD_TOKEN environment variable.");
            System.exit(1);
        }

        String hostname = "127.0.0.1";
        int port = 8080; // Default port

        for (String arg : args) {
            String[] parts = arg.split("=", 2);
            if (parts.length < 2) {
                LOGGER.error("Invalid argument format: {}", arg);
                System.exit(1);
            }

            String key = parts[0];
            String value = parts[1];

            switch (key) {
                case "hostname":
                    if (value.isEmpty()) {
                        LOGGER.error("Hostname cannot be empty.");
                        System.exit(1);
                    }
                    hostname = value;
                    break;
                case "port":
                    try {
                        port = Integer.parseInt(value);
                        if (port <= 0 || port > 65535) {
                            LOGGER.error("Port must be a valid number between 1 and 65535.");
                            System.exit(1);
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.error("Invalid port number: {}", value, e);
                        System.exit(1);
                    }
                    break;
                default:
                    LOGGER.warn("Unknown argument: {}", key);
                    break;
            }
        }

        storage = new SqliteStorage();

        jda = JDABuilder.create(
                        token,
                        Set.of(GatewayIntent.GUILD_INVITES)
                )
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.VOICE_STATE,
                        CacheFlag.EMOJI,
                        CacheFlag.STICKER,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.SCHEDULED_EVENTS
                )
                .build();

        jda.awaitReady();

        jda.getGuilds().parallelStream().forEach(guild -> storage.saveDefaultGuildSettings(guild.getIdLong()));
        // TODO make sure to save default settings for guilds bot joins on runtime

        discordInviteManager = new DiscordInviteManager(jda);

        try {
            apiServer = new ApiServer(hostname, port);
        } catch (IOException e) {
            LOGGER.error("Failed to start the API server on {}:{}", hostname, port, e);
            System.exit(1);
        }

        LOGGER.info("Welcome to the Inviter Application!");
    }

    public static DiscordInviteManager getDiscordInviteMenager() {
        return discordInviteManager;
    }

    public static Storage getStorage() {
        return storage;
    }

}
