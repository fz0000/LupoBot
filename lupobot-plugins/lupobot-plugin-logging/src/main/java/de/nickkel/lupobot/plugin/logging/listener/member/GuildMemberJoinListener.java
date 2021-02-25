package de.nickkel.lupobot.plugin.logging.listener.member;

import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.log.LogEvent;
import de.nickkel.lupobot.plugin.logging.LupoLoggingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildMemberJoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(event.getMember().getUser().getAsTag() + " (" + event.getMember().getId() + ")",
                null, event.getMember().getUser().getAvatarUrl());
        builder.setColor(LupoColor.GREEN.getColor());

        LupoLoggingPlugin.getInstance().sendLog(LogEvent.MEMBER_JOIN, event.getGuild(), builder);
    }
}
