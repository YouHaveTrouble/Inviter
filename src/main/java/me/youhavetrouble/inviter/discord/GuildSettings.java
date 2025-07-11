package me.youhavetrouble.inviter.discord;

import org.jetbrains.annotations.Nullable;

public record GuildSettings(
        long guildId,
        boolean apiEnabled,
        @Nullable String apiHostname
) {

}
