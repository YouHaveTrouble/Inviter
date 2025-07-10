package me.youhavetrouble.inviter.discord;

import org.jetbrains.annotations.Nullable;

public record GuildSettings(
        boolean apiEnabled,
        @Nullable String apiHostname
) {

}
