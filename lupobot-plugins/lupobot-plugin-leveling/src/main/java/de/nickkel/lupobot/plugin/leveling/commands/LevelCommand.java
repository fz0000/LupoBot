package de.nickkel.lupobot.plugin.leveling.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.leveling.LupoLevelingPlugin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "level", aliases = {"rank", "xp"}, category = "general", cooldown = 10)
@SlashOption(name = "member", type = OptionType.USER, required = false)
public class LevelCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        Member member = context.getMember();
        if (context.getArgs().length == 1 || context.getSlash() != null) {
            if (context.getSlash() == null) {
                member = context.getServer().getMember(context.getArgs()[0]);
            } else {
                member = context.getSlash().getOption("member").getAsMember();
            }
            if (member == null) {
                sendSyntaxError(context, "leveling_level-member-not-found");
                return;
            }
        }

        int percent = (int) (Math.round(LupoLevelingPlugin.getInstance().getXP(context.getServer(), context.getUser()) * 100) /
                        LupoLevelingPlugin.getInstance().getRequiredXP(LupoLevelingPlugin.getInstance().getLevel(context.getServer(), context.getUser())+1));
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getTime());
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(member.getUser().getAsTag() + " (" + member.getIdLong() + ")", null, member.getUser().getAvatarUrl());

        builder.setDescription(context.getServer().translate(context.getPlugin(), "leveling_level-progress",
                "```" + getProgressBar(percent) + "```"));
        builder.addField(context.getServer().translate(context.getPlugin(), "leveling_level-level"),
                String.valueOf(LupoLevelingPlugin.getInstance().getLevel(context.getServer(), context.getUser())), false);
        builder.addField(context.getServer().translate(context.getPlugin(), "leveling_level-xp"), context.getServer().formatLong(LupoLevelingPlugin.getInstance().getXP(context.getServer(), context.getUser())) + "/"
                + context.getServer().formatLong(LupoLevelingPlugin.getInstance().getRequiredXP(LupoLevelingPlugin.getInstance().getLevel(context.getServer(), context.getUser())+1)), false);
        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }

    private String getProgressBar(int percent){
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < 50; i++) {
            if (i < (percent/2)) {
                bar.append("=");
            } else if (i == (percent/2)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }

        bar.append("]   " + percent + "%     ");
        return bar.toString();
    }
}