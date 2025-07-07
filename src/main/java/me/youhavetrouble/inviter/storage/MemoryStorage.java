package me.youhavetrouble.inviter.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.youhavetrouble.inviter.DiscordInvite;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class MemoryStorage implements Storage {

    private final Cache<String, DiscordInvite> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.of(60, ChronoUnit.SECONDS))
            .build();

    private final JDA jda;

    public MemoryStorage(JDA jda) {
        this.jda = jda;
    }


    @Nullable
    @Override
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

    @NotNull
    @Override
    public DiscordInvite saveInvite(Invite invite) {

        if (invite == null) {
            throw new IllegalArgumentException("Invite cannot be null");
        }
        if (invite.getGuild() == null) {
            throw new IllegalArgumentException("Invite must be associated with a guild");
        }

        DiscordInvite discordInvite = new DiscordInvite(
                invite.getCode(),
                invite.getGuild().getIdLong(),
                invite.getTimeCreated().toEpochSecond()
        );

        cache.put(invite.getGuild().getId(), discordInvite);
        return discordInvite;
    }
}
