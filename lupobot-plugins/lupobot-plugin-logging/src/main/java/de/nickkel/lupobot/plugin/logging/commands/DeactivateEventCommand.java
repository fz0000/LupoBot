package de.nickkel.lupobot.plugin.logging.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "deactivateevent", aliases = "de", category = "general", permissions = Permission.ADMINISTRATOR)
@SlashOption(name = "event", type = OptionType.STRING)
public class DeactivateEventCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 1) {
            String key;
            if (context.getSlash() == null) {
                key = args[0];
            } else {
                key = context.getSlash().getOption("event").getAsString();
            }
            LogEvent event = null;
            for (LogEvent logEvent : LogEvent.values()) {
                if (logEvent.getKey().equalsIgnoreCase(key)) {
                    event = logEvent;
                }
            }

            if (event == null) {
                sendSyntaxError(context, "logging_deactivateevent-invalid-event");
                return;
            }

            context.getServer().appendPluginData(context.getPlugin(), event.getKey(), -1);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "logging_deactivateevent-info", event.getKey()));
            builder.setTimestamp(context.getTime());
            builder.setColor(LupoColor.RED.getColor());
            send(context, builder);
        } else {
            sendHelp(context);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
