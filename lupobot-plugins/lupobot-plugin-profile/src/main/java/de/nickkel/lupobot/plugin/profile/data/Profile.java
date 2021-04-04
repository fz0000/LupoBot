package de.nickkel.lupobot.plugin.profile.data;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

public class Profile {

    @Getter
    private final LupoUser user;
    @Getter
    private final LupoPlugin plugin;

    public Profile(Member member) {
        this.plugin = LupoBot.getInstance().getPlugin("profile");
        this.user = LupoUser.getByMember(member);
    }
}
