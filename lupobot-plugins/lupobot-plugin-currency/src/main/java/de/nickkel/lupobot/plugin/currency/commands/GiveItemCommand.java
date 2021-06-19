package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.entities.Item;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "giveitem", category = "items")
@SlashOption(name = "user", type = OptionType.USER)
@SlashOption(name = "amount", type = OptionType.INTEGER)
@SlashOption(name = "item", type = OptionType.STRING)
public class GiveItemCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 3) {
            Member giveMember = context.getMember();
            CurrencyUser giveUser = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());

            Member receiveMember;
            if (context.getSlash() == null) {
                receiveMember = context.getServer().getMember(context.getArgs()[0]);
            } else {
                receiveMember = context.getSlash().getOption("user").getAsMember();
            }
            if (receiveMember == null) {
                sendSyntaxError(context, "currency_giveitem-invalid-user");
                return;
            }
            CurrencyUser receiveUser = LupoCurrencyPlugin.getInstance().getCurrencyUser(receiveMember);

            Item item;
            if (context.getSlash() == null) {
                item = LupoCurrencyPlugin.getInstance().getItem(context.getArgs()[1]);
            } else {
                item = LupoCurrencyPlugin.getInstance().getItem(context.getSlash().getOption("item").getAsString());
            }
            if (item == null) {
                sendSyntaxError(context, "currency_giveitem-invalid-item");
                return;
            }

            long amount;
            try {
                if (context.getSlash() == null) {
                    amount = Long.parseLong(context.getArgs()[2]);
                } else {
                    amount = context.getSlash().getOption("amount").getAsLong();
                }
            } catch (NumberFormatException e) {
                sendSyntaxError(context, "currency_giveitem-invalid-amount");
                return;
            }
            if (amount <= 0) {
                sendSyntaxError(context, "currency_giveitem-too-low-amount");
                return;
            }
            if (giveUser.getItem(item)-amount < 0) {
                sendSyntaxError(context, "currency_giveitem-not-enough-items", context.getServer().formatLong(giveUser.getItem(item)));
                return;
            }

            giveUser.addItem(item, -amount);
            receiveUser.addItem(item, amount);

            if (receiveUser.getUsedInventorySlots()+amount > receiveUser.getInventorySlots()) {
                sendSyntaxError(context, "currency_giveitem-receiver-no-space", giveMember.getAsMention(), context.getServer().formatLong(receiveUser.getInventorySlots()), context.getServer().formatLong(amount));
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getTime());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(giveMember.getUser().getAsTag() + " (" + giveMember.getIdLong() + ")", null, giveMember.getUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_giveitem-success", receiveMember.getAsMention()));

            builder.addField(context.getServer().translate(context.getPlugin(), "currency_giveitem-item"), context.getServer().formatLong(amount) + "x " + item.getName(), true);
            builder.addField(context.getServer().translate(context.getPlugin(), "currency_giveitem-worth"), context.getServer().formatLong(amount*item.getBuy()), true);
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
