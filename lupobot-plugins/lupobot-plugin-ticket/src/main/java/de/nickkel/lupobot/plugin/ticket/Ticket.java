package de.nickkel.lupobot.plugin.ticket;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.ticket.enums.TicketState;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.time.OffsetDateTime;

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
        this.id = String.valueOf(this.getTicketData("id"));
        this.author = this.getTicketLong("author");
    }

    public void sendNotify(EmbedBuilder builder) {
        if (this.getNotifyChannel() != null) {
            builder.setTimestamp(OffsetDateTime.now());
            this.getNotifyChannel().sendMessage(builder.build()).queue();
        }
    }

    public TicketState getState() {
        return TicketState.valueOf((String) this.getTicketData("state"));
    }

    public void delete() {
        this.server.getTickets().remove(this.channel);
        ((BasicDBObject) this.server.getServer().getPluginData(this.server.getPlugin(), "tickets")).remove(String.valueOf(this.channel));
        this.server.getGuild().getTextChannelById(this.channel).delete().queue();
    }

    public void close(Member member) {
        TextChannel channel = this.server.getGuild().getTextChannelById(this.channel);
        if (channel != null) {
            if (this.server.getCategory(TicketState.CLOSED) != null) {
                channel.getManager().setParent(this.server.getCategory(TicketState.CLOSED)).complete();
                channel.getManager().sync().queue();
            }
            channel.sendMessage(new EmbedBuilder()
                    .setColor(LupoColor.RED.getColor())
                    .setDescription(this.server.getServer().translate(this.server.getPlugin(), "ticket_close-description", member.getAsMention()))
                    .setFooter(member.getUser().getAsTag() + " (" + member.getId() + ")", member.getUser().getAvatarUrl())
                    .build()
            ).queue(message -> {
                message.addReaction("\uD83D\uDDD1").queue();
            });
        }
        this.setState(TicketState.CLOSED);

        String assignee = "/";
        if (getAssignee() != -1) {
            assignee = member.getGuild().getMemberById(getAssignee()).getAsMention();
        }
        sendNotify(new EmbedBuilder()
                .setColor(LupoColor.RED.getColor())
                .setAuthor(member.getUser().getAsTag() + " (" + member.getId() + ")", null, member.getUser().getAvatarUrl())
                .setDescription(this.server.getServer().translate(this.server.getPlugin(), "ticket_notify-close", member.getAsMention()))
                .addField(this.server.getServer().translate(this.server.getPlugin(), "ticket_notify-owner"), member.getGuild().getMemberById(this.author).getAsMention(), false)
                .addField(this.server.getServer().translate(this.server.getPlugin(), "ticket_notify-channel"), channel.getAsMention(), false)
                .addField(this.server.getServer().translate(this.server.getPlugin(), "ticket_notify-assignee"), assignee, false)
        );
    }

    public void assign(Member member) {
        if (this.getAssignee() != member.getIdLong()) {
            TextChannel channel = member.getGuild().getTextChannelById(this.channel);
            if (channel != null) {
                if (this.server.getCategory(TicketState.ASSIGNED) != null) {
                    channel.getManager().setParent(this.server.getCategory(TicketState.ASSIGNED)).complete();
                    channel.getManager().sync().queue();
                    channel.getManager().getChannel().createPermissionOverride(member).setAllow(Permission.VIEW_CHANNEL).queue();
                }
                channel.getManager().setTopic(server.getServer().translate(server.getPlugin(), "ticket_assignee", member.getAsMention())).queue();
                channel.sendMessage(new EmbedBuilder()
                        .setColor(LupoColor.BLUE.getColor())
                        .setDescription(this.server.getServer().translate(this.server.getPlugin(), "ticket_assign-description", member.getAsMention()))
                        .setFooter(member.getUser().getAsTag() + " (" + member.getId() + ")", member.getUser().getAvatarUrl())
                        .build()
                ).queue();
            }
            this.appendTicketData("assignee", member.getIdLong());
            this.setState(TicketState.ASSIGNED);
        }
    }

    private void setState(TicketState state) {
        this.appendTicketData("state", state.name());
    }

    public long getAssignee() {
        return this.getTicketLong("assignee");
    }

    public TextChannel getNotifyChannel() {
        long id = this.server.getServer().getPluginLong(this.server.getPlugin(), "notifyChannel");
        if (id != -1) {
            TextChannel channel = this.server.getGuild().getTextChannelById(id);
            if (channel != null) {
                return channel;
            } else {
                this.server.getServer().appendPluginData(this.server.getPlugin(), "notifyChannel", -1);
            }
        }
        return null;
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
        channel.getManager().sync().complete();
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
        message.addReaction("\uD83D\uDC64").queue();

        BasicDBObject tickets = (BasicDBObject) server.getPluginData(ticketServer.getPlugin(), "tickets");

        BasicDBObject ticketObject = new BasicDBObject();
        ticketObject.append("id", id);
        ticketObject.append("state", TicketState.OPENED.name());
        ticketObject.append("author", author.getIdLong());
        ticketObject.append("assignee", -1);

        tickets.append(channel.getId(), ticketObject);
        server.appendPluginData(ticketServer.getPlugin(), "tickets", tickets);

        Ticket ticket = Ticket.getByChannel(channel);
        ticket.sendNotify(new EmbedBuilder()
                .setColor(LupoColor.GREEN.getColor())
                .setAuthor(author.getUser().getAsTag() + " (" + author.getId() + ")", null, author.getUser().getAvatarUrl())
                .setDescription(ticketServer.getServer().translate(ticketServer.getPlugin(), "ticket_notify-create"))
                .addField(ticketServer.getServer().translate(ticketServer.getPlugin(), "ticket_notify-owner"), author.getAsMention(), false)
                .addField(ticketServer.getServer().translate(ticketServer.getPlugin(), "ticket_notify-channel"), channel.getAsMention(), false)
        );

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
