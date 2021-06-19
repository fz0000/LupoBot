package de.nickkel.lupobot.plugin.logging.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import de.nickkel.lupobot.plugin.logging.LupoLoggingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@CommandInfo(name = "events", aliases = "e", category = "general", permissions = Permission.MANAGE_SERVER)
public class EventsCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(context.getServer().translate(context.getPlugin(), "logging_events-title"));
        builder.setDescription(context.getServer().translate(context.getPlugin(), "logging_events-info"));
        builder.setTimestamp(context.getTime());
        builder.setColor(LupoColor.ORANGE.getColor());

        for (LogEvent event : LogEvent.values()) {
            long channelId = LupoLoggingPlugin.getInstance().getChannelId(event, context.getGuild());
            String key = context.getServer().translate(context.getPlugin(), "logging_events-key", event.getKey());
            if (channelId == -1) {
               builder.addField(":x: " + context.getServer().translate(context.getPlugin(), event.getLocale())
                        + " " + context.getServer().translate(context.getPlugin(), "logging_events-deactivated"), key, false);
            } else {
                builder.addField(":white_check_mark: " + context.getServer().translate(context.getPlugin(), event.getLocale())
                        + " " + context.getServer().translate(context.getPlugin(), "logging_events-activated"),
                        key + "\n" + context.getGuild().getTextChannelById(channelId).getAsMention() + " (" + channelId + ")",false);
            }
        }

        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
