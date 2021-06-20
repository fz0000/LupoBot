package de.nickkel.lupobot.plugin.roles.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.roles.LupoRolesPlugin;
import de.nickkel.lupobot.plugin.roles.RolesServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.HashMap;
import java.util.Map;

@CommandInfo(name = "reactionroles", category = "reactionroles", permissions = Permission.ADMINISTRATOR)
public class ReactionRolesCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgsAsString().split("\n").length < 2) {
            sendHelp(context);
            return;
        }

        String firstLine = context.getArgsAsString().split("\n")[0];
        String content = firstLine;

        Message message = null;
        try {
            message = context.getChannel().retrieveMessageById(firstLine.split(" ")[0]).complete();
            content = firstLine.replace(message.getId() + " ", "");
        } catch (Exception ignored) {
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(context.getServer().translate(context.getPlugin(), "roles_reactionroles-title"));
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setDescription(content);

        Map<String, Long> reactionRoles = new HashMap<>();
        for (int i = 1; i < context.getArgsAsString().split("\n").length; i++) {
            String line = context.getArgsAsString().split("\n")[i];
            String emoji = line.split(" ")[0];
            Role role = context.getServer().getRole(line.replace(emoji + " ", ""));
            if (role != null) {
                reactionRoles.put(emoji, role.getIdLong());
                builder.addField(role.getName(), emoji, true);
            }
        }

        if (builder.getFields().size() == 0) {
            sendHelp(context);
            return;
        }

        if (message == null) {
            message = context.getChannel().sendMessage(builder.build()).complete();
        }

        RolesServer server = LupoRolesPlugin.getInstance().getRolesServer(context.getGuild());
        server.addReactionRoleMessage(message, reactionRoles);
        context.getMessage().addReaction("✅").queue();
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        send(context, context.getServer().translate(null, "core_command-not-available-slash", context.getCommand().getInfo().name()));
    }
}