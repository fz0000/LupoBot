package de.nickkel.lupobot.plugin.roles.listener;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.plugin.roles.LupoRolesPlugin;
import de.nickkel.lupobot.plugin.roles.RolesServer;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageReactionAddListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        RolesServer server = LupoRolesPlugin.getInstance().getRolesServer(event.getGuild());
        if (((BasicDBObject) server.getServer().getPluginData(server.getPlugin(), "reactionRoles")).containsKey(event.getMessageId())) {
            BasicDBObject reactionRole = (BasicDBObject) ((BasicDBObject) server.getServer().getPluginData(server.getPlugin(), "reactionRoles")).get(event.getMessageId());
            if (reactionRole.containsKey(event.getReactionEmote().getAsReactionCode())) {
                Role role = event.getGuild().getRoleById(reactionRole.getLong(event.getReaction().getReactionEmote().getAsReactionCode()));
                if (role != null) {
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                }
            }
        }
    }
}
