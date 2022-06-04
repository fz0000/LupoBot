package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.*;
import de.nickkel.lupobot.core.pagination.Page;
import de.nickkel.lupobot.core.pagination.Paginator;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.entities.Item;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "shop", category = "items")
@SlashSubCommand(name = "see")
@SlashSubCommand(name = "sellall")
@SlashSubCommand(name = "sell", options = {
        @SlashOption(name = "item", type = OptionType.STRING),
        @SlashOption(name = "amount", type = OptionType.INTEGER, required = false)
})
@SlashSubCommand(name = "buy", options = {
        @SlashOption(name = "item", type = OptionType.STRING),
        @SlashOption(name = "amount", type = OptionType.INTEGER, required = false)
})
public class ShopCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 2 || context.getArgs().length == 3 || context.getSlash() != null) {
            CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());

            if ((context.getSlash() != null && context.getSlash().getSubcommandName().equalsIgnoreCase("sellall"))
                    || (context.getArgs().length == 2 && context.getArgs()[0].equalsIgnoreCase("sell") && context.getArgs()[1].equalsIgnoreCase("all"))) {
                long coins = 0;
                long items = 0;
                for (Item item : LupoCurrencyPlugin.getInstance().getItems()) {
                    if (user.getItem(item) != 0) {
                        coins = coins+user.getItem(item)*item.getSell();
                        items = items+user.getItem(item);
                        user.addItem(item, -user.getItem(item));
                    }
                }

                if (coins == 0) {
                    sendSyntaxError(context, "currency_shop-sell-all-empty");
                    return;
                }
                user.addCoins(coins);

                EmbedBuilder builder = new EmbedBuilder();
                builder.setTimestamp(context.getTime());
                builder.setColor(LupoColor.GREEN.getColor());
                builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());
                builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_shop-sell-all-success", items, context.getServer().formatLong(coins)));
                send(context, builder);
                return;
            }

            Item item;
            if (context.getSlash() == null) {
                item = LupoCurrencyPlugin.getInstance().getItem(context.getArgs()[1]);
            } else {
                item = LupoCurrencyPlugin.getInstance().getItem(context.getSlash().getOption("item").getAsString());
            }
            if (item == null) {
                sendSyntaxError(context, "currency_shop-invalid-item");
                return;
            }

            long amount;
            if (context.getArgs().length == 2 || (context.getSlash() != null && context.getSlash().getOption("amount") == null)) {
                amount = 1;
            } else {
                try {
                    if (context.getSlash() == null) {
                        if (context.getArgs()[0].equalsIgnoreCase("sell") && context.getArgs()[2].equalsIgnoreCase("all")) {
                            amount = user.getItem(item);
                        } else {
                            amount = Long.parseLong(context.getArgs()[2]);
                        }
                    } else {
                        amount = context.getSlash().getOption("amount").getAsLong();
                    }
                } catch (NumberFormatException e) {
                    sendSyntaxError(context, "currency_shop-invalid-amount");
                    return;
                }
            }

            if (amount <= 0) {
                sendSyntaxError(context, "currency_shop-too-low-amount");
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getTime());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());

            long coins = 0;
            if ((context.getSlash() == null && context.getArgs()[0].equalsIgnoreCase("buy"))
                    || (context.getSlash() != null && context.getSlash().getSubcommandName().equalsIgnoreCase("buy"))) {
                coins = amount*item.getBuy();
                if (user.getCoins()-coins < 0) {
                    sendSyntaxError(context, "currency_shop-buy-not-enough-coins", context.getServer().formatLong(user.getCoins()));
                    return;
                }
                if (user.getUsedInventorySlots()+amount > user.getInventorySlots()) {
                    sendSyntaxError(context, "currency_shop-buy-not-enough-slots", context.getServer().formatLong(user.getInventorySlots()), context.getServer().formatLong(amount));
                    return;
                }
                user.addCoins(-coins);
                user.addItem(item, amount);
                builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_shop-buy"));
            } else if ((context.getSlash() == null && context.getArgs()[0].equalsIgnoreCase("sell"))
                    || (context.getSlash() != null && context.getSlash().getSubcommandName().equalsIgnoreCase("sell"))) {
                if (user.getItem(item)-amount < 0) {
                    sendSyntaxError(context, "currency_shop-sell-not-enough-items", context.getServer().formatLong(user.getItem(item)));
                    return;
                }
                coins = amount*item.getSell();
                user.addCoins(coins);
                user.addItem(item, -amount);
                builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_shop-sell"));
            }

            builder.addField(context.getServer().translate(context.getPlugin(), "currency_shop-item"), context.getServer().formatLong(amount) + "x " + item.getName(), true);
            builder.addField(context.getServer().translate(context.getPlugin(), "currency_shop-coins"), context.getServer().formatLong(coins), true);
            send(context, builder);
        } else {
            ArrayList<Page> pages = new ArrayList<>();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setFooter(context.getServer().translate(context.getPlugin(), "currency_shop-footer"));
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setThumbnail("https://i.imgur.com/3xNz0s9.png");
            builder.setAuthor(context.getServer().translate(context.getPlugin(), "currency_shop-title"), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_shop-buy-sell"));

            for (int i=0; i < LupoCurrencyPlugin.getInstance().getItems().size(); i++) {
                Item item  = LupoCurrencyPlugin.getInstance().getItems().get(i);
                builder.addField(item.getIcon() + " " + item.getName(), context.getServer().translate(context.getPlugin(), "currency_shop-buy-sell-item",
                        context.getServer().formatLong(item.getBuy()), context.getServer().formatLong(item.getSell())), true);
                if (String.valueOf(i).length() != 1 && (String.valueOf(i).endsWith("0") || i == LupoCurrencyPlugin.getInstance().getItems().size()-1)) {
                    Page page = new Page(builder.build());
                    page.getWhitelist().add(context.getMember().getIdLong());
                    pages.add(page);
                    builder.clearFields();
                }
            }

            Paginator.paginate(context, pages, 90);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
