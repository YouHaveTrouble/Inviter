package me.youhavetrouble.inviter.discord.listener;

import me.youhavetrouble.inviter.Main;
import me.youhavetrouble.inviter.storage.Storage;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildJoinAndLeaveListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Storage storage = Main.getStorage();
        long guildId = event.getGuild().getIdLong();
        storage.removeGuildSettings(guildId);
        storage.saveDefaultGuildSettings(guildId);
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        long guildId = event.getGuild().getIdLong();
        Main.getDiscordInviteMenager().removeFromCache(guildId);
        Main.getStorage().removeGuildSettings(guildId);
    }

}
