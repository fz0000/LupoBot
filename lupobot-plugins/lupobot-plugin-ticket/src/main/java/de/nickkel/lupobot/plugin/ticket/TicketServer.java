package de.nickkel.lupobot.plugin.ticket;

import com.mongodb.BasicDBList;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.plugin.ticket.enums.TicketState;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;

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
    private Map<Long, Ticket> tickets = new HashMap<>();

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
        BasicDBList dbList = new BasicDBList();
        dbList.addAll((ArrayList) this.server.getPluginData(this.plugin, "supportTeamRoles"));
        for (Object id : dbList) {
            if (this.guild.getRoleById((long) id) != null) {
                roles.add(this.guild.getRoleById((long) id));
            } else { // remove role if it doesn't exist anymore
                dbList.remove(id);
                this.server.appendPluginData(this.plugin, "supportTeamRoles", dbList);
            }
        }
        return roles;
    }

    public long getCreationMessage() {
        return this.server.getPluginLong(this.plugin, "creationMessage");
    }

    public TextChannel getNotifyChannel() {
        long notifyChannel = this.server.getPluginLong(this.plugin, "notifyChannel");
        if (notifyChannel != -1) {
            if (this.guild.getTextChannelById(notifyChannel) != null) {
                return this.guild.getTextChannelById(notifyChannel);
            }
        }
        return null;
    }

    public Category getCategory(TicketState state) {
        long category = this.server.getPluginLong(this.plugin, state.getKey());
        if (category != -1) {
            return this.guild.getCategoryById(category);
        }
        return null;
    }
}
