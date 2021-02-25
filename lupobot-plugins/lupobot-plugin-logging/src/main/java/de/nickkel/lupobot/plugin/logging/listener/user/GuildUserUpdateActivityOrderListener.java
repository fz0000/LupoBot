package de.nickkel.lupobot.plugin.logging.listener.user;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import de.nickkel.lupobot.plugin.logging.LupoLoggingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivityOrderEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildUserUpdateActivityOrderListener extends ListenerAdapter {

    @Override
    public void onUserUpdateActivityOrder(@NotNull UserUpdateActivityOrderEvent event) {
        LupoServer server = LupoServer.getByGuild(event.getGuild());
        LupoPlugin plugin = LupoBot.getInstance().getPlugin("logging");

        String old = "/";
        if(event.getOldValue().size() == 1) {
            old = event.getOldValue().get(0).getName();
        }
        String current = "/";
        if(event.getOldValue().size() == 1) {
            current = event.getNewValue().get(0).getName();
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(event.getMember().getUser().getAsTag() + " (" + event.getMember().getId() + ")",
                null, event.getMember().getUser().getAvatarUrl());
        builder.addField(server.translate(plugin, "logging_activity-old"), old, false);
        builder.addField(server.translate(plugin, "logging_activity-new"), current, false);
        builder.setColor(LupoColor.ORANGE.getColor());

        LupoLoggingPlugin.getInstance().sendLog(LogEvent.ACTIVITY_UPDATE, event.getGuild(), builder);
    }
}