package de.nickkel.lupobot.core.command;

import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CommandContext {

    @Getter
    private final Member member;
    @Getter
    private final TextChannel channel;
    @Getter
    private final Message message;
    @Getter
    private final Guild guild;
    @Getter
    private final LupoServer server;
    @Getter
    private final LupoUser user;
    @Getter @Setter
    private LupoPlugin plugin;
    @Getter
    private final String[] args;
    @Getter
    private final String label;

    public CommandContext(Member member, TextChannel channel, Message message, String label, String[] args) {
        this.member = member;
        this.channel = channel;
        this.message = message;
        this.args = args;
        this.label = label;
        this.guild = message.getGuild();
        this.server = LupoServer.getByGuild(this.guild);
        this.user = LupoUser.getByMember(this.member);
    }

    public String getArgsAsString() {
        String argString = "";
        for (String arg : this.args) {
            argString = argString + arg + " ";
        }
        return argString;
    }
}
