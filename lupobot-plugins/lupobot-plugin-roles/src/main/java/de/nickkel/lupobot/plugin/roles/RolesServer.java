package de.nickkel.lupobot.plugin.roles;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RolesServer {

    @Getter
    private final Guild guild;
    @Getter
    private final LupoServer server;
    @Getter
    private final LupoPlugin plugin;

    public RolesServer(Guild guild) {
        this.plugin = LupoBot.getInstance().getPlugin("roles");
        this.guild = guild;
        this.server = LupoServer.getByGuild(guild);
    }

    public void addReactionRoleMessage(Message message, Map<String, Long> roles) {
        BasicDBObject reactionRoles = (BasicDBObject) this.server.getPluginData(this.plugin, "reactionRoles");
        BasicDBObject reactionRole = new BasicDBObject();

        for (String emoji : roles.keySet()) {
            reactionRole.append(emoji, roles.get(emoji));
            message.addReaction(emoji).queue();
        }
        reactionRoles.append(message.getId(), reactionRole);
    }

    public List<Role> getSelfAssignRoles() {
        List<Role> roles = new ArrayList<>();
        BasicDBList dbList = new BasicDBList();
        dbList.addAll((ArrayList) this.server.getPluginData(this.plugin, "selfAssignRoles"));
        for (Object id : dbList) {
            if (this.guild.getRoleById((long) id) != null) {
                roles.add(this.guild.getRoleById((long) id));
            } else { // remove role if it doesn't exist anymore
                dbList.remove(id);
                this.server.appendPluginData(this.plugin, "selfAssignRoles", dbList);
            }
        }
        return roles;
    }
}
