package de.nickkel.lupobot.plugin.ticket.listener;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.plugin.ticket.LupoTicketPlugin;
import de.nickkel.lupobot.plugin.ticket.Ticket;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelDeleteListener extends ListenerAdapter {

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        LupoServer server = LupoServer.getByGuild(event.getGuild());
        if (((BasicDBObject) server.getPluginData(LupoBot.getInstance().getPlugin(LupoTicketPlugin.getInstance().getInfo().name()), "tickets")).containsKey(event.getChannel().getId())) {
            Ticket ticket = new Ticket(event.getChannel());
            ticket.delete();
        }
    }
}