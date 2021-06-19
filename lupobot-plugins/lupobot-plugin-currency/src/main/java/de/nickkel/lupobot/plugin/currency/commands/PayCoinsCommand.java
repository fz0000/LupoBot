package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "paycoins", category = "general")
@SlashOption(name = "user", type = OptionType.USER)
@SlashOption(name = "amount", type = OptionType.INTEGER)
public class PayCoinsCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 2 || context.getSlash() != null) {
            Member receiveMember;
            if (context.getSlash() == null) {
                receiveMember = context.getServer().getMember(context.getArgs()[0]);
            } else {
                receiveMember = context.getSlash().getOption("user").getAsMember();
            }
            if (receiveMember == null) {
                sendSyntaxError(context, "currency_paycoins-member-not-found");
                return;
            }

            CurrencyUser receiveUser = LupoCurrencyPlugin.getInstance().getCurrencyUser(receiveMember);
            CurrencyUser payUser = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());

            long coins;
            try {
                if (context.getSlash() == null) {
                    coins = Long.parseLong(context.getArgs()[1]);
                } else {
                    coins = context.getSlash().getOption("amount").getAsLong();
                }
            } catch (NumberFormatException e) {
                sendSyntaxError(context, "currency_paycoins-invalid-number");
                return;
            }

            if (payUser.getCoins()-coins < 0) {
                sendSyntaxError(context, "currency_paycoins-not-enough-coins");
                return;
            }

            payUser.addCoins(-coins);
            receiveUser.addCoins(coins);

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getTime());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + payUser.getUser().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_paycoins-amount", context.getServer().formatLong(coins), receiveMember.getAsMention()));
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