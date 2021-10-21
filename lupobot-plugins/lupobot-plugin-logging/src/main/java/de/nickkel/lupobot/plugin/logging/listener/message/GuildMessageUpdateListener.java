package de.nickkel.lupobot.plugin.logging.listener.message;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import de.nickkel.lupobot.plugin.logging.LupoLoggingPlugin;
import de.nickkel.lupobot.plugin.logging.log.LogMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildMessageUpdateListener extends ListenerAdapter {

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (event.getMessage().getAuthor().isBot()) {
            return;
        }
        LogMessage message = new LogMessage(event.getMessage().getIdLong(), true);
        try {
            LupoServer server = LupoServer.getByGuild(event.getGuild());
            LupoPlugin plugin = LupoBot.getInstance().getPlugin("logging");

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(event.getMember().getUser().getAsTag() + " (" + event.getMember().getId() + ")",
                    null, event.getMember().getUser().getAvatarUrl());
            builder.addField(server.translate(plugin, "logging_message-channel"), event.getChannel().getAsMention()
                    + " (" + event.getChannel().getId() + ")", false);
            builder.addField(server.translate(plugin, "logging_message-id"), event.getMessageId(), false);
            builder.addField(server.translate(plugin, "logging_message-old"), message.get("content"), false);
            builder.addField(server.translate(plugin, "logging_message-new"), event.getMessage().getContentRaw(), false);
            builder.setColor(LupoColor.GREEN.getColor());

            LupoLoggingPlugin.getInstance().sendLog(LogEvent.MESSAGE_UPDATE, event.getGuild(), builder);
        } catch (Exception ignored) {
        }
        message.update(event.getMessage());
    }
}
