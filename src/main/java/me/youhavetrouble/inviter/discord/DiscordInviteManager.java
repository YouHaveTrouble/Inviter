package me.youhavetrouble.inviter.discord;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DiscordInviteManager {

    private final Cache<String, DiscordInvite> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.of(60, ChronoUnit.SECONDS))
            .build();

    private final JDA jda;

    public DiscordInviteManager(JDA jda) {
        this.jda = jda;
    }

    @Nullable
    public DiscordInvite getInvite(long guildId) {
        DiscordInvite discordInvite = cache.getIfPresent(String.valueOf(guildId));
        if (discordInvite == null || discordInvite.isExpired()) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                return null; // Guild not found
            }
            DefaultGuildChannelUnion defaultChannel = guild.getDefaultChannel();
            if (defaultChannel == null) {
                return null; // No default channel found
            }
            Invite invite = defaultChannel.createInvite()
                    .setMaxAge(60) // Set the invite to expire after 60 seconds
                    .complete();
            if (invite == null) return null; // Failed to create invite
            discordInvite = new DiscordInvite(
                    invite.getCode(),
                    guild.getIdLong(),
                    invite.getTimeCreated().toEpochSecond()
            );
        }
        return discordInvite;
    }

}
