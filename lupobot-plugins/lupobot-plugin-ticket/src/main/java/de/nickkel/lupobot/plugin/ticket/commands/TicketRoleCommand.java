package de.nickkel.lupobot.plugin.ticket.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.ticket.LupoTicketPlugin;
import de.nickkel.lupobot.plugin.ticket.TicketServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(name = "ticketrole", category = "config", permissions = Permission.ADMINISTRATOR)
@SlashOption(name = "role", type = OptionType.ROLE)
public class TicketRoleCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        TicketServer server = LupoTicketPlugin.getInstance().getTicketServer(context.getGuild());
        if (context.getArgs().length == 1 || context.getSlash() != null) {
            Role role;
            if (context.getSlash() != null) {
                role = context.getSlash().getOption("role").getAsRole();
            } else {
                role = context.getServer().getRole(context.getArgsAsString());
            }

            if (role == null) {
                sendSyntaxError(context, "ticket_ticketrole-invalid-role");
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.setTimestamp(context.getTime());

            List<Role> roles = new ArrayList<>(server.getSupportTeamRoles());
            if (!roles.contains(role)) {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketrole-add"));
                builder.setColor(LupoColor.GREEN.getColor());
                roles.add(role);
            } else {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "ticket_ticketrole-remove"));
                builder.setColor(LupoColor.RED.getColor());
                roles.remove(role);
            }
            List<Long> roleIds = new ArrayList<>();
            for (Role all : roles) {
                roleIds.add(all.getIdLong());
            }
            context.getServer().appendPluginData(context.getPlugin(), "supportTeamRoles", roleIds);

            builder.addField(context.getServer().translate(context.getPlugin(), "ticket_ticketrole-role"),
                    role.getAsMention() + " (" + role.getId() + ")", false);
            send(context, builder);
        } else {
            sendHelp(context);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
