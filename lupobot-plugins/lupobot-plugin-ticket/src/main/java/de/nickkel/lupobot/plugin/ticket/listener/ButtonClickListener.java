package de.nickkel.lupobot.plugin.ticket.listener;

import de.nickkel.lupobot.plugin.ticket.LupoTicketPlugin;
import de.nickkel.lupobot.plugin.ticket.Ticket;
import de.nickkel.lupobot.plugin.ticket.TicketServer;
import de.nickkel.lupobot.plugin.ticket.enums.TicketState;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonClickListener extends ListenerAdapter {

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        User user = event.getUser();
        if (!user.isBot()) {
            TicketServer server = LupoTicketPlugin.getInstance().getTicketServer(event.getGuild());
            if (event.getMessageIdLong() == server.getCreationMessage() && event.getMember() != null && event.getComponentId().equals("TICKET;CREATE")) {
                event.deferEdit().queue();
                Ticket.create(event.getTextChannel(), event.getMember());
            }

            Ticket ticket = Ticket.getByChannel(event.getTextChannel());
            if (ticket != null) {
                if (event.getComponentId().equals("TICKET;CLOSE") && ticket.getState() != TicketState.CLOSED) {
                    event.deferEdit().queue();
                    ticket.close(event.getMember());
                } else if (event.getComponentId().equals("TICKET;CLAIM") && ticket.getState() != TicketState.CLOSED) {
                    if (event.getMember().getRoles().contains(server.getSupportTeamRoles()) || event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        event.deferEdit().queue();
                        ticket.assign(event.getMember());
                    }
                } else if (event.getComponentId().equals("TICKET;DELETE")) {
                    event.deferEdit().queue();
                    ticket.delete();
                }
            }
        }
    }
}
