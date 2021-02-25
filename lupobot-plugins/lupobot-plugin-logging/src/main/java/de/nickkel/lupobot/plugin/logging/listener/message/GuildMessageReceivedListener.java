package de.nickkel.lupobot.plugin.logging.listener.message;

import de.nickkel.lupobot.plugin.logging.log.LogMessage;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageReceivedListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        LogMessage message = new LogMessage(event.getMessage().getIdLong(), true);
        message.update(event.getMessage());
    }
}
