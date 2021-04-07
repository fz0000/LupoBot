package de.nickkel.lupobot.plugin.logging.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

@CommandInfo(name = "activateevent", aliases = "ae", category = "general", permissions = Permission.ADMINISTRATOR)
public class ActivateEventCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 0) {
            sendHelp(context);
            return;
        }

        String key = args[0];
        TextChannel channel;
        if (args.length == 1) {
            channel = context.getChannel();
        } else if (args.length == 2) {
            channel = context.getServer().getTextChannel(args[1]);
        } else {
            sendHelp(context);
            return;
        }

        LogEvent event = null;
        for (LogEvent logEvent : LogEvent.values()) {
            if (logEvent.getKey().equalsIgnoreCase(key)) {
                event = logEvent;
            }
        }

        if (event == null) {
            sendSyntaxError(context, "logging_activateevent-invalid-event");
            return;
        } else if (channel == null) {
            sendSyntaxError(context, "logging_activateevent-invalid-channel");
            return;
        }

        context.getServer().appendPluginData(context.getPlugin(), event.getKey(), channel.getIdLong());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "logging_activateevent-info"));
        builder.addField(context.getServer().translate(context.getPlugin(), "logging_activateevent-channel"),
                channel.getAsMention() + " (" + channel.getId() + ")", false);
        builder.addField(context.getServer().translate(context.getPlugin(), "logging_activateevent-event"),
                context.getServer().translate(context.getPlugin(), event.getLocale()), false);
        builder.setTimestamp(context.getMessage().getTimeCreated());
        builder.setColor(LupoColor.GREEN.getColor());
        context.getChannel().sendMessage(builder.build()).queue();
    }
}
