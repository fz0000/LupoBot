package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

@CommandInfo(name = "coins", category = "general")
public class CoinsCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        Member member = context.getMember();
        if (context.getArgs().length == 1) {
            member = context.getServer().getMember(context.getArgs()[0]);
            if (member == null) {
                sendSyntaxError(context, "currency_coins-member-not-found");
                return;
            }
        }

        CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(member);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setColor(LupoColor.ORANGE.getColor());
        builder.setAuthor(member.getUser().getAsTag() + " (" + member.getIdLong() + ")", null, member.getUser().getAvatarUrl());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_coins-amount",
                context.getServer().formatLong(user.getCoins())));

        context.getChannel().sendMessage(builder.build()).queue();
    }
}