package de.nickkel.lupobot.core.internal.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

@CommandInfo(name = "plugins", aliases = "listplugins")
public class PluginsCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#6495ED"));
        builder.setTitle(context.getServer().translate(null, "core_plugin-list"));
        builder.setDescription(context.getServer().translate(null, "core_plugin-list-description"));

        for(LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
            String statusKey = "core_plugin-status-uninstalled";
            if(context.getServer().getPlugins().contains(plugin)) {
                statusKey = "core_plugin-status-installed";
            }
            builder.addField(context.getServer().translatePluginName(plugin), context.getServer().translate(null, statusKey) + "\n"
                    + context.getServer().translate(plugin, plugin.getInfo().name() + "_description"), false);
        }

        context.getChannel().sendMessage(builder.build()).queue();
    }
}
