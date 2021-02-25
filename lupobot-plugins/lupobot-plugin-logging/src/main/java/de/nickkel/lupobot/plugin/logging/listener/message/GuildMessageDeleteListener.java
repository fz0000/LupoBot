package de.nickkel.lupobot.plugin.logging.listener.message;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.LupoLoggingPlugin;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import de.nickkel.lupobot.plugin.logging.log.LogMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageDeleteListener extends ListenerAdapter {

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        LogMessage message = new LogMessage(event.getMessageIdLong(), false);
        try {
            LupoServer server = LupoServer.getByGuild(event.getGuild());
            LupoPlugin plugin = LupoBot.getInstance().getPlugin("logging");

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(message.get("authorAsTag") + " (" + message.get("authorId") + ")",
                    null, message.get("authorAvatarUrl"));
            builder.addField(server.translate(plugin, "logging_message-channel"), event.getChannel().getAsMention()
                    + " (" + event.getChannel().getId() + ")", false);
            builder.addField(server.translate(plugin, "logging_message-id"), event.getMessageId(), false);
            builder.addField(server.translate(plugin, "logging_message-content"), message.get("content"), false);
            builder.setColor(LupoColor.RED.getColor());

            LupoLoggingPlugin.getInstance().sendLog(LogEvent.MESSAGE_DELETE, event.getGuild(), builder);
            message.delete();
        } catch(Exception ignored) {
        }
    }
}
