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

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "addselfrole", category = "self-assign-roles", permissions = Permission.ADMINISTRATOR)
public class AddSelfRoleCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        RolesServer server = LupoRolesPlugin.getInstance().getRolesServer(context.getGuild());
        if (context.getArgs().length == 1) {
            Role role = context.getServer().getRole(context.getArgs()[0]);
            if (role == null) {
                sendSyntaxError(context, "roles_addselfrole-invalid-role");
                return;
            }

            if (server.getSelfAssignRoles().contains(role)) {
                sendSyntaxError(context, "roles_addselfrole-already-exists");
            } else {
                List<Long> roleIds = new ArrayList<>();
                for (Role all : server.getSelfAssignRoles()) {
                    roleIds.add(all.getIdLong());
                }
                roleIds.add(role.getIdLong());
                context.getServer().appendPluginData(context.getPlugin(), "selfAssignRoles", roleIds);

                context.getChannel().sendMessage(new EmbedBuilder()
                        .setColor(LupoColor.GREEN.getColor())
                        .setTimestamp(context.getMessage().getTimeCreated())
                        .setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl())
                        .setDescription(context.getServer().translate(context.getPlugin(), "roles_addselfrole-success", role.getAsMention(), role.getId()))
                        .build()
                ).queue();
            }
        } else {
            sendHelp(context);
        }
    }
}
