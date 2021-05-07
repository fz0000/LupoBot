package de.nickkel.lupobot.plugin.ticket.listener;

import de.nickkel.lupobot.plugin.ticket.LupoTicketPlugin;
import de.nickkel.lupobot.plugin.ticket.Ticket;
import de.nickkel.lupobot.plugin.ticket.TicketServer;
import de.nickkel.lupobot.plugin.ticket.enums.TicketState;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageReactionAddListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        User user = event.retrieveUser().complete();
        if (!user.isBot()) {
            TicketServer server = LupoTicketPlugin.getInstance().getTicketServer(event.getGuild());
            if (event.getMessageIdLong() == server.getCreationMessage()) {
                if (!event.getReaction().isSelf()) {
                    event.getReaction().removeReaction(user).queue();
                    Ticket.create(event.getChannel(), event.getMember());
                }
            }

            Ticket ticket = Ticket.getByChannel(event.getChannel());
            if (ticket != null) {
                if (event.getReaction().getReactionEmote().getAsReactionCode().equals("🔒") && ticket.getState() != TicketState.CLOSED) {
                    event.getReaction().removeReaction(user).queue();
                    ticket.close(event.getMember());
                } else if (event.getReaction().getReactionEmote().getAsReactionCode().equals("👤") && ticket.getState() != TicketState.CLOSED) {
                    if (event.getMember().getRoles().contains(server.getSupportTeamRoles()) || event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        event.getReaction().removeReaction(user).queue();
                        ticket.assign(event.getMember());
                    }
                } else if (event.getReaction().getReactionEmote().getAsReactionCode().equals("🗑")) {
                    ticket.delete();
                }
            }
        }
    }
}
