package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.pagination.method.Pages;
import de.nickkel.lupobot.core.pagination.model.Page;
import de.nickkel.lupobot.core.pagination.type.PageType;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.data.Item;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "shop", category = "items")
public class ShopCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 2 || context.getArgs().length == 3) {
            CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());
            Item item = LupoCurrencyPlugin.getInstance().getItem(context.getArgs()[1]);
            if(item == null) {
                sendSyntaxError(context, "currency_shop-invalid-item");
                return;
            }

            long amount = 0;
            if(context.getArgs().length == 2) {
                amount = 1;
            } else {
                try {
                    if(context.getArgs()[0].equalsIgnoreCase("sell") && context.getArgs()[2].equalsIgnoreCase("all")) {
                        amount = user.getItem(item);
                    } else {
                        amount = Long.parseLong(context.getArgs()[2]);
                    }
                } catch(NumberFormatException e) {
                    sendSyntaxError(context, "currency_shop-invalid-amount");
                    return;
                }
            }

            if(amount <= 0) {
                sendSyntaxError(context, "currency_shop-too-low-amount");
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());

            long coins = 0;
            if(context.getArgs()[0].equalsIgnoreCase("buy")) {
                if(user.getCoins()-amount < 0) {
                    sendSyntaxError(context, "currency_shop-buy-not-enough-coins", context.getServer().formatLong(user.getCoins()));
                    return;
                }
                if(user.getUsedInventorySlots()+amount > user.getInventorySlots()) {
                    sendSyntaxError(context, "currency_shop-buy-not-enough-slots", context.getServer().formatLong(user.getInventorySlots()), context.getServer().formatLong(amount));
                    return;
                }
                coins = amount*item.getBuy();
                user.addCoins(-coins);
                user.addItem(item, amount);
                builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_shop-buy"));
            } else if(context.getArgs()[0].equalsIgnoreCase("sell")) {
                if(user.getItem(item)-amount < 0) {
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
            context.getChannel().sendMessage(builder.build()).queue();
        } else {
            ArrayList<Page> pages = new ArrayList<>();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setFooter(context.getServer().translate(context.getPlugin(), "currency_shop-footer"));
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setThumbnail("https://i.imgur.com/3xNz0s9.png");
            builder.setAuthor(context.getServer().translate(context.getPlugin(), "currency_shop-title"), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_shop-buy-sell"));

            for(int i=0; i < LupoCurrencyPlugin.getInstance().getItems().size(); i++) {
                if(String.valueOf(i).length() != 1 && (String.valueOf(i).endsWith("0") || i == LupoCurrencyPlugin.getInstance().getItems().size()-1)) {
                    pages.add(new Page(PageType.EMBED, builder.build()));
                    builder.clearFields();
                }
                Item item  = LupoCurrencyPlugin.getInstance().getItems().get(i);
                builder.addField(item.getIcon() + " " + item.getName(), context.getServer().translate(context.getPlugin(), "currency_shop-buy-sell-item",
                        context.getServer().formatLong(item.getBuy()), context.getServer().formatLong(item.getSell())), true);
            }

            context.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
                Pages.paginate(success, pages, 60, TimeUnit.SECONDS, user -> context.getUser().getId() == user.getIdLong());
            });
        }
    }
}
