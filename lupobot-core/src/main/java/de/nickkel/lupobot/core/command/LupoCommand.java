package de.nickkel.lupobot.core.command;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.awt.*;

public abstract class LupoCommand {

    @Getter
    private final CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);

    public abstract void onCommand(CommandContext context);

    public void sendHelp(CommandContext context) {
        LupoServer server = LupoServer.getByGuild(context.getGuild());
        LupoPlugin plugin = context.getPlugin();

        String title = server.getPrefix() + this.info.name();
        if(this.info.aliases().length != 0) {
            for(String alias : this.info.aliases()) {
                title = title + " / " + server.getPrefix() + alias;
            }
        }
        String permissions = "/";
        if(this.info.permissions().length != 0) {
            permissions = "";
            for(Permission permission : this.info.permissions()) {
                permissions = permissions + "\n" + permission.toString();
            }
        }

        String pluginName = "core";
        if(plugin != null) {
            pluginName = plugin.getInfo().name();
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#6495ED"));
        builder.setTitle(title);
        builder.setDescription(server.translate(context.getPlugin(), pluginName + "_" + this.getInfo().name() + "-description"));
        builder.addField(server.translate(null, "core_command-usage"), server.translate(plugin, pluginName + "_" + this.getInfo().name() + "-usage"), false);
        builder.addField(server.translate(null, "core_command-permission"), permissions, false);
        builder.addField(server.translate(null, "core_command-example"), server.translate(plugin, pluginName + "_" + this.getInfo().name() + "-example"), false);
        builder.setFooter(server.translate(null, "core_command-plugin") + ": " +
                server.translatePluginName(plugin));
        context.getChannel().sendMessage(builder.build()).queue();
    }
}
