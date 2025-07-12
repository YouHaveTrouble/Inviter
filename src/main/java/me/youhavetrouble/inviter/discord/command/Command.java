package me.youhavetrouble.inviter.discord.command;

import me.youhavetrouble.inviter.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class Command extends ListenerAdapter {

    private static final Map<String, Command> commands = new HashMap<>();

    public abstract void register(JDA jda, String name);

    public abstract void execute(SlashCommandInteractionEvent event);

    public abstract String getName();

    boolean canUse(User user) {
        return true;
    }

    public static void registerCommand(Command command) {
        String name = command.getName();
        if (commands.containsKey(name)) {
            Main.LOGGER.warn("Command {} is already registered.", name);
            return;
        }
        command.register(Main.getJda(), name);
        commands.put(name, command);
    }

    public static void executeCommand(SlashCommandInteractionEvent event, String name) {
        Command command = commands.get(name.toLowerCase(Locale.ENGLISH));
        if (command == null) {
            event.reply("Unknown command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!command.canUse(event.getUser())) {
            event.reply("You do not have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        command.execute(event);
    }

}
