package de.nickkel.lupobot.plugin.leveling.listener;

import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.plugin.leveling.LupoLevelingPlugin;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageReceivedListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        LupoServer server = LupoServer.getByGuild(event.getGuild());
        if (event.getMember() == null) {
            return;
        }

        LupoUser user = LupoUser.getByMember(event.getMember());
        if (LupoLevelingPlugin.getInstance().isReadyToReceiveXP(server, user)) {
            LupoLevelingPlugin.getInstance().addXP(server, user, LupoLevelingPlugin.getInstance().getRandomXP(), event.getChannel());
        }
    }
}
