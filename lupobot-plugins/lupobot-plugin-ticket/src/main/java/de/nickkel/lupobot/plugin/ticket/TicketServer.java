package de.nickkel.lupobot.plugin.ticket;

import com.mongodb.BasicDBList;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketServer {

    @Getter
    private final Guild guild;
    @Getter
    private final LupoServer server;
    @Getter
    private final LupoPlugin plugin;
    @Getter
    private final Map<TextChannel, Ticket> tickets = new HashMap<>();

    public TicketServer(Guild guild) {
        this.plugin = LupoBot.getInstance().getPlugin("ticket");
        this.guild = guild;
        this.server = LupoServer.getByGuild(guild);
    }

    public int getLimitAmount() {
        return (int) this.server.getPluginData(this.plugin, "limitAmount");
    }

    public boolean isVisibleEveryone() {
        return (boolean) this.server.getPluginData(this.plugin, "visibleEveryone");
    }

    public List<Role> getSupportTeamRoles() {
        List<Role> roles = new ArrayList<>();
        BasicDBList dbList = (BasicDBList) this.server.getPluginData(this.plugin, "supportTeamRoles");
        for(Object id : dbList) {
            if(this.guild.getRoleById((long) id) != null) {
                roles.add(this.guild.getRoleById((long) id));
            } else { // remove role if it doesn't exist anymore
                dbList.remove(id);
                this.server.appendPluginData(this.plugin, "supportTeamRoles", dbList);
            }
        }
        return roles;
    }
}
