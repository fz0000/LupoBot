package de.nickkel.lupobot.core.data;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class LupoUser {

    @Getter
    private final long id;
    @Getter
    private final User discordUser;
    @Getter
    private final Map<LupoCommand, Long> cooldowns = new HashMap<>();

    public LupoUser(long id) {
        this.id = id;
        this.discordUser = LupoBot.getInstance().getShardManager().getUserById(id);
        LupoBot.getInstance().getLogger().info("Loading user " + discordUser.getAsTag() + " " + id + " ...");
        LupoBot.getInstance().getUsers().put(this.id, this);
    }

    public static LupoUser getByMember(Member member) {
        if(LupoBot.getInstance().getUsers().containsKey(member.getIdLong())) {
            return LupoBot.getInstance().getUsers().get(member.getIdLong());
        } else {
            return new LupoUser(member.getIdLong());
        }
    }
}
