package me.youhavetrouble.inviter.storage;


import me.youhavetrouble.inviter.discord.GuildSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Storage {

    @NotNull GuildSettings getGuildSettings(long guildId);

    @Nullable GuildSettings getGuildSettings(@NotNull String hostname);

    void saveDefaultGuildSettings(long guildId);

    void removeGuildSettings(long guildId);

    void updateDiscordApiEnabled(long guildId, boolean enabled);

    void addHostname(long guildId, @Nullable String hostname);

    void removeHostname(@NotNull String hostname);

    List<String> listHostnames(long guildId);

    void cleanUpHostnames();

}
