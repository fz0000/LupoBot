package de.nickkel.lupobot.plugin.roles.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.roles.LupoRolesPlugin;
import de.nickkel.lupobot.plugin.roles.RolesServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "removeselfrole", category = "self-assign-roles", permissions = Permission.ADMINISTRATOR)
@SlashOption(name = "role", type = OptionType.ROLE)
public class RemoveSelfRoleCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        RolesServer server = LupoRolesPlugin.getInstance().getRolesServer(context.getGuild());
        if (context.getArgs().length == 1 || context.getSlash() != null) {
            Role role;
            if (context.getSlash() == null) {
                role = context.getServer().getRole(context.getArgsAsString());
            } else {
                role = context.getSlash().getOption("role").getAsRole();
            }

            if (role == null) {
                sendSyntaxError(context, "roles_removeselfrole-invalid-role");
                return;
            }

            if (!server.getSelfAssignRoles().contains(role)) {
                sendSyntaxError(context, "roles_removeselfrole-not-exists");
            } else {
                List<Long> roleIds = new ArrayList<>();
                for (Role all : server.getSelfAssignRoles()) {
                    roleIds.add(all.getIdLong());
                }
                roleIds.remove(role.getIdLong());
                context.getServer().appendPluginData(context.getPlugin(), "selfAssignRoles", roleIds);

                send(context, new EmbedBuilder()
                        .setColor(LupoColor.GREEN.getColor())
                        .setTimestamp(context.getTime())
                        .setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl())
                        .setDescription(context.getServer().translate(context.getPlugin(), "roles_removeselfrole-success", role.getAsMention(), role.getId()))
                        .build()
                );
            }
        } else {
            sendHelp(context);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
