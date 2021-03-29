package de.nickkel.lupobot.plugin.leveling.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.leveling.LupoLevelingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@CommandInfo(name = "addxp", category = "administration", permissions = Permission.ADMINISTRATOR, cooldown = 5)
public class AddXpCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 2) {
            Member member = context.getServer().getMember(context.getArgs()[0]);
            if(member == null) {
                sendSyntaxError(context, "leveling_addxp-member-not-found");
                return;
            }

            long xp = 0;
            try {
                xp = Long.parseLong(context.getArgs()[1]);
            } catch(NumberFormatException e) {
                sendSyntaxError(context, "leveling_addxp-only-numbers");
                return;
            }

            if(xp < 0) {
                sendSyntaxError(context, "leveling_addxp-only-positive-numbers");
                return;
            }

            LupoLevelingPlugin.getInstance().addXP(context.getServer(), context.getUser(), xp);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(member.getUser().getAsTag() + " (" + member.getIdLong() + ")", null, member.getUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "leveling_addxp-success", context.getServer().formatLong(xp)));
            builder.addField(context.getServer().translate(context.getPlugin(), "leveling_addxp-new-xp"),
                    context.getServer().formatLong(LupoLevelingPlugin.getInstance().getXP(context.getServer(), context.getUser())), false);
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            sendHelp(context);
        }
    }
}