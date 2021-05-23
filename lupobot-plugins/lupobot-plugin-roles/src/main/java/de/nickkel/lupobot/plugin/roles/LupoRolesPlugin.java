package de.nickkel.lupobot.plugin.roles;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.core.util.ListenerRegister;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

@PluginInfo(name = "roles", version = "1.0.0", author = "Nickkel")
public class LupoRolesPlugin extends LupoPlugin {

    @Getter
    public static LupoRolesPlugin instance;
    private final Map<Long, RolesServer> rolesServer = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        LupoBot.getInstance().getCommandHandler().registerCommands(this, "de.nickkel.lupobot.plugin.roles.commands");
        new ListenerRegister(this, "de.nickkel.lupobot.plugin.roles.listener");
    }

    @Override
    public void onDisable() {

    }

    public RolesServer getRolesServer(Guild guild) {
        return this.rolesServer.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            return new RolesServer(LupoBot.getInstance().getShardManager().getGuildById(guildId));
        });
    }
}
