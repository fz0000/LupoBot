package de.nickkel.lupobot.plugin.logging.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.LogEvent;
import de.nickkel.lupobot.plugin.logging.LupoLoggingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

@CommandInfo(name = "events", aliases = "e", category = "general", permissions = Permission.MANAGE_SERVER)
public class EventsCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(context.getServer().translate(context.getPlugin(), "logging_events-title"));
        builder.setDescription(context.getServer().translate(context.getPlugin(), "logging_events-description"));
        builder.setTimestamp(context.getMessage().getTimeCreated());
        builder.setColor(LupoColor.ORANGE.getColor());

        for(LogEvent event : LogEvent.values()) {
            long channelId = LupoLoggingPlugin.getInstance().getChannelId(event, context.getGuild());
            String key = context.getServer().translate(context.getPlugin(), "logging_events-key", event.getKey());
            if(channelId == -1) {
               builder.addField(":x: " + context.getServer().translate(context.getPlugin(), event.getLocale())
                        + " " + context.getServer().translate(context.getPlugin(), "logging_events-deactivated"), key, false);
            } else {
                builder.addField(":white_check_mark: " + context.getServer().translate(context.getPlugin(), event.getLocale())
                        + " " + context.getServer().translate(context.getPlugin(), "logging_events-activated"),
                        key + "\n" + context.getGuild().getTextChannelById(channelId).getAsMention() + " (" + channelId + ")",false);
            }
        }

        context.getChannel().sendMessage(builder.build()).queue();
    }
}
