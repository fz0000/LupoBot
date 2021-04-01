package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

@CommandInfo(name = "paycoins", category = "general")
public class PayCoinsCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 2) {
            Member receiveMember = context.getServer().getMember(context.getArgs()[0]);
            if(receiveMember == null) {
                sendSyntaxError(context, "currency_paycoins-member-not-found");
                return;
            }

            CurrencyUser receiveUser = LupoCurrencyPlugin.getInstance().getCurrencyUser(receiveMember);
            CurrencyUser payUser = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());

            long coins = 0;
            try {
                coins = Long.parseLong(context.getArgs()[1]);
            } catch(NumberFormatException e) {
                sendSyntaxError(context, "currency_paycoins-invalid-number");
                return;
            }

            if(payUser.getCoins()-coins < 0) {
                sendSyntaxError(context, "currency_paycoins-not-enough-coins");
                return;
            }

            payUser.addCoins(-coins);
            receiveUser.addCoins(coins);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + payUser.getUser().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_paycoins-amount", context.getServer().formatLong(coins), receiveMember.getAsMention()));
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            sendHelp(context);
        }
    }
}