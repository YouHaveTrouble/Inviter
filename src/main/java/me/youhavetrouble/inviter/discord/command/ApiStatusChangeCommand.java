package me.youhavetrouble.inviter.discord.command;

import me.youhavetrouble.inviter.Main;
import me.youhavetrouble.inviter.discord.GuildSettings;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

public class ApiStatusChangeCommand extends Command {

    @Nullable
    public String getName() {
        return "invites";
    }

    public void register(JDA jda, String name) {
        jda.upsertCommand(Commands.slash("invites", "Change or see if Inviter should create invites for this guild.")
                .setIntegrationTypes(IntegrationType.GUILD_INSTALL)
                .addOptions(
                        new OptionData(OptionType.BOOLEAN, "status", "Enable or disable Inviter to work for this guild", false)
                )
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
        ).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping statusMapping = event.getOption("status");

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a guild.").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        if (statusMapping == null) {
            GuildSettings setings = Main.getStorage().getGuildSettings(guild.getIdLong());

            String message = setings.invitesEnabled() ?
                "Inviter is currently __**enabled**__ for this server." :
                "Inviter is currently __**disabled**__ for this server.";

            event.getHook().editOriginal(message).queue();
            return;
        }

        boolean status = statusMapping.getAsBoolean();
        long guildId = guild.getIdLong();

        Main.getStorage().updateInvitesEnabled(guildId, status);
        String message = status ? "Inviter is now __**enabled**__ for this server." : "Inviter API is now __**disabled**__ for this server.";
        event.getHook().editOriginal(message).queue();
    }

}
