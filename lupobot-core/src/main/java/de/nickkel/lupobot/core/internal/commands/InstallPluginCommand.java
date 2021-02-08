package de.nickkel.lupobot.core.internal.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.time.LocalDateTime;

@CommandInfo(name = "installplugin", permissions = Permission.MANAGE_SERVER, cooldown = 5, category = "core")
public class InstallPluginCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1) {
            if(LupoBot.getInstance().getPlugins().size() == 0) {
                context.getChannel().sendMessage("**Error:** No plugins loaded! Nothing to install").queue();
                return;
            }
            for(LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
                if(context.getArgs()[0].equalsIgnoreCase(plugin.getInfo().name()) || context.getArgs()[0].equalsIgnoreCase(context.getServer().translatePluginName(plugin))) {
                    if(context.getServer().getPlugins().contains(plugin)) {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setColor(LupoColor.RED.getColor());
                        builder.setAuthor(context.getGuild().getName(), null, context.getGuild().getIconUrl());
                        builder.setDescription(context.getServer().translate(null, "core_plugin-already-installed", context.getServer().translatePluginName(plugin)));
                        builder.setTimestamp(LocalDateTime.now());
                        context.getChannel().sendMessage(builder.build()).queue();
                        return;
                    } else {
                        context.getServer().getPlugins().add(plugin);
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setColor(LupoColor.GREEN.getColor());
                        builder.setAuthor(context.getGuild().getName(), null, context.getGuild().getIconUrl());
                        builder.setDescription(context.getServer().translate(null, "core_plugin-installed", context.getServer().translatePluginName(plugin)));
                        context.getChannel().sendMessage(builder.build()).queue();
                        return;
                    }
                }
            }
        } else {
            sendHelp(context);
        }
    }
}
