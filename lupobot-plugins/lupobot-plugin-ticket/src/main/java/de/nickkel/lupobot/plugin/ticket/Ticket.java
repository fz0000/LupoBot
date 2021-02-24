package de.nickkel.lupobot.plugin.ticket;

import de.nickkel.lupobot.core.data.LupoServer;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Ticket {

    @Getter
    private final LupoServer server;
    @Getter
    private final Guild guild;
    @Getter
    private TextChannel channel;
    @Getter
    private final Member author;
    @Getter
    private Member assignee;

    public Ticket(LupoServer server, Member author) {
        this.server = server;
        this.author = author;
        this.guild = this.server.getGuild();
        this.channel.getManager().getChannel().createPermissionOverride(this.guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
    }

    public void assign(Member assignee) {
        this.assignee = assignee;
    }
}
