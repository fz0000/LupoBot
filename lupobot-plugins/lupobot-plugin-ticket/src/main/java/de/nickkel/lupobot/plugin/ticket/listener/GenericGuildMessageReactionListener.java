package de.nickkel.lupobot.plugin.ticket.listener;

import de.nickkel.lupobot.plugin.ticket.LupoTicketPlugin;
import de.nickkel.lupobot.plugin.ticket.Ticket;
import de.nickkel.lupobot.plugin.ticket.TicketServer;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GenericGuildMessageReactionListener extends ListenerAdapter {

    @Override
    public void onGenericGuildMessageReaction(GenericGuildMessageReactionEvent event) {
        TicketServer server = LupoTicketPlugin.getInstance().getTicketServer(event.getGuild());
        if (event.getMessageIdLong() == server.getCreationMessage()) {
            if (event.getMember() != null && !event.getReaction().isSelf()) {
                if (event.getReaction().hasCount()) {
                    event.getReaction().removeReaction().queue();
                }
                Ticket.create(event.getChannel(), event.getMember());
            }
        }
    }
}
