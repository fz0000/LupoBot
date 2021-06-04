package de.nickkel.lupobot.core.command;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        LupoServer server = LupoServer.getByGuild(event.getGuild());
        String prefix = server.getPrefix();

        if (event.getMessage().getContentRaw().replace("!", "").equals(LupoBot.getInstance().getSelfUser().getAsMention())) {
            LupoCommand command = LupoBot.getInstance().getCommand("help");
            if (command != null) {
                CommandContext context = new CommandContext(event.getMember(), event.getChannel(), event.getMessage(), "prefix", new String[]{});
                context.setPlugin(LupoBot.getInstance().getPlugin("help"));
                command.onCommand(context);
            }
        }

        if (!event.getMessage().getContentRaw().startsWith(prefix) || event.getMessage().getContentRaw().equals(prefix)) {
            return;
        }

        String label = event.getMessage().getContentStripped().replace(prefix, "").split(" ")[0];
        String message = event.getMessage().getContentRaw().replace(prefix, "").replace(label, "");
        if (message.startsWith(" ")) {
            message = message.substring(1);
        }

        String[] args = message.split(" ");
        if (Arrays.toString(args).equals("[]")) {
            args = new String[0];
        }
        CommandContext context = new CommandContext(event.getMember(), event.getChannel(), event.getMessage(), label, args);
        LupoBot.getInstance().getCommandHandler().runCommand(context);
    }
}
