package de.nickkel.lupobot.plugin.leveling.commands;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.leveling.LupoLevelingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

@CommandInfo(name = "rewardrole", category = "administration", permissions = Permission.ADMINISTRATOR)
public class RewardRoleCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        LupoServer server = context.getServer();

        if (context.getArgs().length == 3 && context.getArgs()[0].equalsIgnoreCase("add")) {
            Role role = context.getServer().getRole(context.getArgs()[1]);
            if (role == null) {
                sendSyntaxError(context, "leveling_rewardrole-invalid-role");
                return;
            }

            long level = 0;
            try {
                level = Long.parseLong(context.getArgs()[2]);
                if (level <= 0) {
                    sendSyntaxError(context, "leveling_rewardrole-small-level");
                    return;
                }
            } catch (NumberFormatException e) {
                sendSyntaxError(context, "leveling_rewardrole-invalid-level");
                return;
            }

            BasicDBObject pluginObject = (BasicDBObject) server.getData().get(LupoLevelingPlugin.getInstance().getInfo().name());
            BasicDBObject rewardObject = (BasicDBObject) pluginObject.get("rewardRoles");
            rewardObject.append(String.valueOf(level), role.getIdLong());

            pluginObject.append("rewardRoles", rewardObject);
            server.getData().append(LupoLevelingPlugin.getInstance().getInfo().name(), pluginObject);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "leveling_rewardrole-success"));
            builder.addField(context.getServer().translate(context.getPlugin(), "leveling_rewardrole-level"), String.valueOf(level), false);
            builder.addField(context.getServer().translate(context.getPlugin(), "leveling_rewardrole-role"), role.getName() + " (" + role.getId() + ")", false);

            context.getChannel().sendMessage(builder.build()).queue();
        } else if (context.getArgs().length == 2 && context.getArgs()[0].equalsIgnoreCase("remove")) {
            long level = 0;
            try {
                level = Long.parseLong(context.getArgs()[1]);
                if (level <= 0) {
                    sendSyntaxError(context, "leveling_rewardrole-small-level");
                    return;
                }
            } catch (NumberFormatException e) {
                sendSyntaxError(context, "leveling_rewardrole-invalid-level");
                return;
            }

            BasicDBObject pluginObject = (BasicDBObject) server.getData().get(LupoLevelingPlugin.getInstance().getInfo().name());
            BasicDBObject rewardObject = (BasicDBObject) pluginObject.get("rewardRoles");

            if (rewardObject.containsKey(String.valueOf(level))) {
                rewardObject.remove(String.valueOf(level));
                pluginObject.append("rewardRoles", rewardObject);
                server.getData().append(this.getInfo().name(), pluginObject);

                EmbedBuilder builder = new EmbedBuilder();
                builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
                builder.setColor(LupoColor.RED.getColor());
                builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());
                builder.setDescription(context.getServer().translate(context.getPlugin(), "leveling_rewardrole-removed"));
                builder.addField(context.getServer().translate(context.getPlugin(), "leveling_rewardrole-level"), String.valueOf(level), false);

                context.getChannel().sendMessage(builder.build()).queue();
            } else {
                sendSyntaxError(context, "leveling_rewardrole-not-exists");
            }
        } else if (context.getArgs().length == 1 && context.getArgs()[0].equalsIgnoreCase("list")) {
            BasicDBObject pluginObject = (BasicDBObject) server.getData().get(LupoLevelingPlugin.getInstance().getInfo().name());
            BasicDBObject rewardObject = (BasicDBObject) pluginObject.get("rewardRoles");

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setAuthor(context.getGuild().getName() + " (" + context.getGuild().getId() + ")", null, context.getGuild().getIconUrl());

            if (rewardObject.keySet().size() == 0) {
                builder.setDescription(context.getServer().translate(context.getPlugin(), "leveling_rewardrole-list-empty"));
                context.getChannel().sendMessage(builder.build()).queue();
                return;
            }

            builder.setDescription(context.getServer().translate(context.getPlugin(), "leveling_rewardrole-list"));
            for (String level : rewardObject.keySet()) {
                if (LupoLevelingPlugin.getInstance().existsRewardRole(server, Long.parseLong(level))) {
                    Role role = context.getServer().getGuild().getRoleById(rewardObject.getLong(level));
                    builder.addField(context.getServer().translate(context.getPlugin(), "leveling_rewardrole-level-list", level), role.getName() + " (" + role.getId() + ")", false);
                }
            }
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            sendHelp(context);
        }
    }
}