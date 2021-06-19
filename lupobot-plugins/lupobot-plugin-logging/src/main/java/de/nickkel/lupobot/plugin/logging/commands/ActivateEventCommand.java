package de.nickkel.lupobot.plugin.logging.commands;

import de.nickkel.lupobot.core.command.*;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "activateevent", aliases = "ae", category = "general", permissions = Permission.ADMINISTRATOR)
@SlashOption(name = "event", type = OptionType.STRING)
@SlashOption(name = "channel", type = OptionType.CHANNEL, required = false)
public class ActivateEventCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 0 && context.getSlash() == null) {
            sendHelp(context);
            return;
        }

        String key;
        TextChannel channel;
        if (context.getSlash() == null) {
            key = args[0];
            if (args.length == 1) {
                channel = context.getChannel();
            } else if (args.length == 2) {
                channel = context.getServer().getTextChannel(args[1]);
            } else {
                sendHelp(context);
                return;
            }
        } else {
            key = context.getSlash().getOption("event").getAsString();
            if (context.getSlash().getOption("channel") != null) {
                channel = (TextChannel) context.getSlash().getOption("channel").getAsMessageChannel();
            } else {
                channel = context.getChannel();
            }
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
        builder.setTimestamp(context.getTime());
        builder.setColor(LupoColor.GREEN.getColor());
        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
