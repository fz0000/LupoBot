package de.nickkel.lupobot.plugin.logging.listener.role;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.logging.LogEvent;
import de.nickkel.lupobot.plugin.logging.LupoLoggingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildMemberRoleAddListener extends ListenerAdapter {

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        LupoServer server = LupoServer.getByGuild(event.getGuild());
        LupoPlugin plugin = LupoBot.getInstance().getPlugin("logging");

        String roles = "";
        for(Role role : event.getRoles()) {
            roles = roles + role.getName() + " (" + role.getId() + ")\n";
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(event.getMember().getUser().getAsTag() + " (" + event.getMember().getId() + ")",
                null, event.getMember().getUser().getAvatarUrl());
        builder.addField(server.translate(plugin, "logging_role-new"), roles, false);
        builder.setColor(LupoColor.GREEN.getColor());

        LupoLoggingPlugin.getInstance().sendLog(LogEvent.ROLE_ADD, event.getGuild(), builder);
    }
}
