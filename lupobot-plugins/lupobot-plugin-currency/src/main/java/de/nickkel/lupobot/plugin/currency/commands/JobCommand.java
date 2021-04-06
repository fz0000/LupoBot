package de.nickkel.lupobot.plugin.currency.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.pagination.method.Pages;
import de.nickkel.lupobot.core.pagination.model.Page;
import de.nickkel.lupobot.core.pagination.type.PageType;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.data.Item;
import de.nickkel.lupobot.plugin.currency.data.Job;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "job", category = "general")
public class JobCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1) {
            CurrencyUser user = LupoCurrencyPlugin.getInstance().getCurrencyUser(context.getMember());
            int id = -1;
            try {
                id = Integer.parseInt(context.getArgs()[0]);
            } catch(NumberFormatException e) {
                sendSyntaxError(context, "currency_job-invalid-id");
                return;
            }
            if(LupoCurrencyPlugin.getInstance().getJobs().size() <= id-1) {
                sendSyntaxError(context, "currency_job-invalid-id");
                return;
            }
            if(user.getCurrentJob() != null) {
                sendSyntaxError(context, "currency_job-already-working");
                return;
            }

            Job job = LupoCurrencyPlugin.getInstance().getJobs().get(id-1);
            if(user.getItem(job.getNeededItem()) == 0) {
                sendSyntaxError(context, "currency_job-no-item", job.getNeededItem().getIcon() + " " + job.getNeededItem().getName(), job.getNeededItem().getName());
                return;
            }
            user.addItem(job.getNeededItem(), -1);
            user.setCurrentJob(job);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());
            builder.setThumbnail(job.getImage());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_job-started"));
            builder.addField(job.getTranslatedName(context.getServer()), job.getTranslatedDescription(context.getServer()), false);
            context.getChannel().sendMessage(builder.build()).queue();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    user.setCurrentJob(null);
                    user.addCoins(job.getCoins());
                    context.getMember().getUser().openPrivateChannel().queue(success -> {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
                        builder.setColor(LupoColor.GREEN.getColor());
                        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getIdLong() + ")", null, context.getMember().getUser().getAvatarUrl());
                        builder.setThumbnail(job.getImage());
                        builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_job-done",
                                TimeUtils.format(context, job.getDuration()*1000L), context.getServer().formatLong(job.getCoins())));
                        builder.addField(job.getTranslatedName(context.getServer()), job.getTranslatedDescription(context.getServer()), false);
                        success.sendMessage(builder.build()).queue();
                    });
                }
            }, job.getDuration()*1000L);
        } else {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
            builder.setColor(LupoColor.AQUA.getColor());
            builder.setAuthor(context.getServer().translate(context.getPlugin(), "currency_job-title"), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_job-choose"));

            ArrayList<Page> pages = new ArrayList<>();
            int id = 1;
            for(Job job : LupoCurrencyPlugin.getInstance().getJobs()) {
                builder.addField(id + ". " + job.getTranslatedName(context.getServer()) + " (" + TimeUtils.format(context, job.getDuration()*1000L) + ")", context.getServer().translate(context.getPlugin(), "currency_job-info",
                        context.getServer().formatLong(job.getCoins()), job.getNeededItem().getIcon() + " " + job.getNeededItem().getName())
                        + job.getTranslatedDescription(context.getServer()), false);
                if(String.valueOf(id).length() != 1 && (String.valueOf(id).endsWith("0") || id == LupoCurrencyPlugin.getInstance().getJobs().size())) {
                    pages.add(new Page(PageType.EMBED, builder.build()));
                    builder.clearFields();
                }
                id++;
            }
            context.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
                Pages.paginate(success, pages, 120, TimeUnit.SECONDS, reactUser -> context.getUser().getId() == reactUser.getIdLong());
            });
        }
    }
}
