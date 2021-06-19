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

@CommandInfo(name = "coins", category = "general")
@SlashOption(name = "user", type = OptionType.USER, required = false)
public class CoinsCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        Member member = context.getMember();
        if (context.getSlash() == null) {
            if (context.getArgs().length == 1) {
                member = context.getServer().getMember(context.getArgs()[0]);
                if (member == null) {
                    sendSyntaxError(context, "currency_coins-member-not-found");
                    return;
                }
            }
        } else {
            if (context.getSlash().getOption("user") != null) {
                member = context.getSlash().getOption("user").getAsMember();
            }
        }

        CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(member);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getTime());
        builder.setColor(LupoColor.ORANGE.getColor());
        builder.setAuthor(member.getUser().getAsTag() + " (" + member.getIdLong() + ")", null, member.getUser().getAvatarUrl());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_coins-amount",
                context.getServer().formatLong(user.getCoins())));

        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}