package me.youhavetrouble.inviter.storage;


import me.youhavetrouble.inviter.discord.GuildSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Storage {

    @NotNull GuildSettings getGuildSettings(long guildId);

    void saveDefaultGuildSettings(long guildId);

    void updateDiscordApiEnabled(long guildId, boolean enabled);

    void updateDiscordApiHostname(long guildId, @Nullable String hostname);

}
