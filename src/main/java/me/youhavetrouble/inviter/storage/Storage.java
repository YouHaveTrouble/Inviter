package me.youhavetrouble.inviter.storage;


import me.youhavetrouble.inviter.discord.GuildSettings;
import org.jetbrains.annotations.NotNull;

public interface Storage {

    @NotNull GuildSettings getGuildSettings(long guildId);

    void saveDefaultGuildSettings(long guildId);

    void removeGuildSettings(long guildId);

    void updateInvitesEnabled(long guildId, boolean enabled);

}
