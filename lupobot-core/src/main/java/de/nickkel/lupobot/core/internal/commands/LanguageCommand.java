package de.nickkel.lupobot.core.internal.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.pagination.method.Pages;
import de.nickkel.lupobot.core.pagination.model.Page;
import de.nickkel.lupobot.core.pagination.type.PageType;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.Locale;

@CommandInfo(name = "language", aliases = "lang", permissions = Permission.ADMINISTRATOR, category = "core")
public class LanguageCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if(context.getArgs().length == 1) {
            if(LupoBot.getInstance().getAvailableLanguages().contains(context.getArgs()[0]) || getLanguageCodeByName(context.getArgs()[0]) != null) {
                String languageCode = context.getArgs()[0];
                if(getLanguageCodeByName(context.getArgs()[0]) != null) {
                    languageCode = getLanguageCodeByName(context.getArgs()[0]);
                }
                Locale locale = new Locale(languageCode.split("_")[0]);
                if(context.getServer().getLanguage().equals(languageCode)) {
                    sendSyntaxError(context, "core_language-already-using", locale.getDisplayName(locale));
                    return;
                }
                context.getServer().setLanguage(languageCode);
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(LupoColor.GREEN.getColor());
                builder.setAuthor(LupoBot.getInstance().getSelfUser().getName(), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
                builder.setDescription(context.getServer().translate(null, "core_language-changed", locale.getDisplayName(locale)));
                builder.setTimestamp(context.getMessage().getTimeCreated());
                context.getChannel().sendMessage(builder.build()).queue();
            } else {
                sendSyntaxError(context, "core_language-not-exists");
            }
        } else {
            ArrayList<Page> pages = new ArrayList<>();

            // Page with all languages listed
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.YELLOW.getColor());
            builder.setTitle(context.getServer().translate(null, "core_available-languages"));
            for(String language : LupoBot.getInstance().getAvailableLanguages()) {
                Locale locale = new Locale(language.split("_")[0]);
                String name = locale.getDisplayName(locale);
                String flag = ":flag_" + language.split("_")[1].toLowerCase() + ":";
                builder.addField(flag + " " + name, language, false);
            }
            pages.add(new Page(PageType.EMBED, builder.build()));

            // Page with help
            pages.add(new Page(PageType.EMBED, getHelpBuilder(context).build()));

            context.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success -> {
                Pages.paginate(success, pages);
            });
        }
    }

    private String getLanguageCodeByName(String name) {
        for(String code : LupoBot.getInstance().getAvailableLanguages()) {
            Locale locale = new Locale(code.split("_")[0]);
            if(name.equalsIgnoreCase(locale.getDisplayName(locale))) {
                return code;
            }
        }
        return null;
    }
}
