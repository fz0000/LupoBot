package de.nickkel.lupobot.plugin.ticket;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;

import java.util.HashMap;
import java.util.Map;

@PluginInfo(name = "ticket", version = "1.0.0", author = "Nickkel")
public class LupoTicketPlugin extends LupoPlugin {

    @Getter
    public static LupoTicketPlugin instance;
    private final Map<Long, TicketServer> ticketServer = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        LupoBot.getInstance().getCommandHandler().registerCommands(this, "de.nickkel.lupobot.plugin.ticket.commands");
    }

    @Override
    public void onDisable() {

    }

    public TicketServer getTicketServer(Guild guild) {
        if(!this.ticketServer.containsKey(guild.getIdLong())) {
            this.ticketServer.put(guild.getIdLong(), new TicketServer(guild));
        }
        return this.ticketServer.get(guild.getIdLong());
    }
}
