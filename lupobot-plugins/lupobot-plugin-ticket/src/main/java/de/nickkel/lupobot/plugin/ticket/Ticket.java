package de.nickkel.lupobot.plugin.ticket;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.StringUtils;
import de.nickkel.lupobot.plugin.ticket.enums.TicketState;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class Ticket {

    @Getter
    private final TicketServer server;
    @Getter
    private final long channel, author;
    @Getter
    private final String id;

    public Ticket(TextChannel channel) {
        this.server = LupoTicketPlugin.getInstance().getTicketServer(channel.getGuild());
        this.channel = channel.getIdLong();
        this.id = (String) this.getTicketData("id");
        this.author = this.getTicketLong("author");
    }

    public TicketState getState() {
        return TicketState.valueOf((String) this.getTicketData("state"));
    }

    public void setState(TicketState state) {
        this.appendTicketData("state", state.name());
    }

    public long getAssignee() {
        return this.getTicketLong("assignee");
    }

    public void assign(long id) {
        this.appendTicketData("assignee", id);
    }

    public static void create(TextChannel origin, Member author) {
        TicketServer ticketServer = LupoTicketPlugin.getInstance().getTicketServer(author.getGuild());
        LupoServer server = ticketServer.getServer();
        long id = server.getPluginLong(ticketServer.getPlugin(), "lastTicketId")+1;
        server.appendPluginData(ticketServer.getPlugin(), "lastTicketId", id);

        Category category = ticketServer.getCategory(TicketState.OPENED);
        TextChannel channel;
        if (category == null) {
            channel = author.getGuild().createTextChannel("ticket-" + id, origin.getParent()).complete();
        } else {
            channel = author.getGuild().createTextChannel("ticket-" + id, category).complete();
        }
        if (!ticketServer.isVisibleEveryone()) {
            channel.getManager().getChannel().createPermissionOverride(author.getGuild().getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
        }
        for (Role role : ticketServer.getSupportTeamRoles()) {
            channel.getManager().getChannel().createPermissionOverride(role).setAllow(Permission.VIEW_CHANNEL).queue();
        }
        Message message = channel.sendMessage(new MessageBuilder()
                .setContent(server.translate(ticketServer.getPlugin(), "ticket_welcome-message", author.getAsMention()))
                .setEmbed(new EmbedBuilder()
                        .setDescription(server.translate(ticketServer.getPlugin(), "ticket_welcome-description"))
                        .setColor(LupoColor.BLUE.getColor())
                        .build())
                .build()
        ).complete();
        message.addReaction("\uD83D\uDD12").queue();

        BasicDBObject tickets = (BasicDBObject) server.getPluginData(ticketServer.getPlugin(), "tickets");

        BasicDBObject ticket = new BasicDBObject();
        ticket.append("id", id);
        ticket.append("state", TicketState.OPENED.name());
        ticket.append("author", author.getIdLong());
        ticket.append("assignee", -1);

        tickets.append(channel.getId(), ticket);
        server.appendPluginData(ticketServer.getPlugin(), "tickets", tickets);

    }

    public static Ticket getByChannel(TextChannel channel) {
        TicketServer server = LupoTicketPlugin.getInstance().getTicketServer(channel.getGuild());
        if (server.getTickets().containsKey(channel.getIdLong())) {
            return server.getTickets().get(channel.getIdLong());
        }
        if (((BasicDBObject) ((BasicDBObject) server.getServer().getData().get(server.getPlugin().getInfo().name())).get("tickets")).containsKey(channel.getId())) {
            return new Ticket(channel);
        }
        return null;
    }

    private void appendTicketData(String key, Object val) {
        BasicDBObject dbObject = ((BasicDBObject) ((BasicDBObject) this.server.getServer().getPluginData(LupoBot.getInstance().getPlugin(LupoTicketPlugin.getInstance().getInfo().name()),
                "tickets")).get(String.valueOf(this.channel))).append(key, val);
        dbObject.append(key, val);
    }

    private Object getTicketData(String key) {
        BasicDBObject dbObject = ((BasicDBObject) ((BasicDBObject) this.server.getServer().getPluginData(LupoBot.getInstance().getPlugin(LupoTicketPlugin.getInstance().getInfo().name()),
                "tickets")).get(String.valueOf(this.channel)));
        return dbObject.get(key);
    }

    private long getTicketLong(String key) {
        BasicDBObject dbObject = ((BasicDBObject) ((BasicDBObject) this.server.getServer().getPluginData(LupoBot.getInstance().getPlugin(LupoTicketPlugin.getInstance().getInfo().name()),
                "tickets")).get(String.valueOf(this.channel)));
        return dbObject.getLong(key);
    }
}
