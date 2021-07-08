package de.nickkel.lupobot.plugin.profile;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.plugin.profile.data.Profile;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.Map;

@PluginInfo(name = "profile", author = "Nickkel")
public class LupoProfilePlugin extends LupoPlugin {

    @Getter
    private static LupoProfilePlugin instance;
    private final Map<Long, Profile> profiles = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands("de.nickkel.lupobot.plugin.profile.commands");
    }

    @Override
    public void onDisable() {

    }

    public Profile getProfile(Member member) {
        if (!this.profiles.containsKey(member.getIdLong())) {
            this.profiles.put(member.getIdLong(), new Profile(member));
        }
        return this.profiles.get(member.getIdLong());
    }
}
