package de.nickkel.lupobot.plugin.roles.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.roles.LupoRolesPlugin;
import de.nickkel.lupobot.plugin.roles.RolesServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "listselfroles", category = "self-assign-roles")
public class ListSelfRolesCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        RolesServer server = LupoRolesPlugin.getInstance().getRolesServer(context.getGuild());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
        builder.setTimestamp(context.getTime());

        if (server.getSelfAssignRoles().size() == 0) {
            builder.setDescription(context.getServer().translate(context.getPlugin(), "roles_listselfroles-none"));
            send(context, builder);
            return;
        }

        String roles = "";
        builder.setDescription(context.getServer().translate(context.getPlugin(), "roles_listselfroles-info"));
        for (Role role : server.getSelfAssignRoles()) {
            roles = role.getAsMention() + " (" + role.getId() + ")\n";
        }
        builder.addField(context.getServer().translate(context.getPlugin(), "roles_listselfroles-title"), roles, false);
        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
