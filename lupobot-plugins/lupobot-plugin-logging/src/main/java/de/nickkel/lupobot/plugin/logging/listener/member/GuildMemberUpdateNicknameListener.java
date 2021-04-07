package de.nickkel.lupobot.plugin.logging.listener.member;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import de.nickkel.lupobot.plugin.logging.LupoLoggingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildMemberUpdateNicknameListener extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        LupoServer server = LupoServer.getByGuild(event.getGuild());
        LupoPlugin plugin = LupoBot.getInstance().getPlugin("logging");

        String old = event.getOldNickname();
        if (event.getOldNickname() == null || event.getOldNickname().equals("")) {
            old = "/";
        }
        String current = event.getNewNickname();
        if (event.getNewNickname() == null || event.getNewNickname().equals("")) {
            current = "/";
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(event.getMember().getUser().getAsTag() + " (" + event.getMember().getId() + ")",
                null, event.getMember().getUser().getAvatarUrl());
        builder.addField(server.translate(plugin, "logging_nickname-old"), old, false);
        builder.addField(server.translate(plugin, "logging_nickname-new"), current, false);
        builder.setColor(LupoColor.ORANGE.getColor());

        LupoLoggingPlugin.getInstance().sendLog(LogEvent.NICKNAME_UPDATE, event.getGuild(), builder);
    }
}