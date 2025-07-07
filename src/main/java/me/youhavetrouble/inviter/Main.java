package me.youhavetrouble.inviter;

import me.youhavetrouble.inviter.storage.MemoryStorage;
import me.youhavetrouble.inviter.storage.Storage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger("Inviter");

    private static JDA jda;

    private static Storage storage;

    public static void main(String[] args) throws InterruptedException {

        String token = System.getenv("DISCORD_TOKEN");

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

        storage = new MemoryStorage(jda);

        LOGGER.info("Welcome to the Inviter Application!");

    }

}
