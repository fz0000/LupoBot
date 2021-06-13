package de.nickkel.lupobot.core.internal.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.pagination.Page;
import de.nickkel.lupobot.core.pagination.Paginator;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.Locale;

@CommandInfo(name = "language", aliases = "lang", permissions = Permission.ADMINISTRATOR, category = "core")
@SlashOption(name = "language", type = OptionType.STRING, choices = {"English", "German"})
public class LanguageCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1 || context.getSlash() != null) {
            String languageCode;
            if (context.getSlash() != null) {
                languageCode = context.getSlash().getOption("language").getAsString();
            } else {
                languageCode = context.getArgs()[0];
            }
            if (LupoBot.getInstance().getAvailableLanguages().contains(languageCode) || getLanguageCodeByName(languageCode) != null) {
                if (getLanguageCodeByName(languageCode) != null) {
                    languageCode = getLanguageCodeByName(context.getArgs()[0]);
                }
                Locale locale = new Locale(languageCode.split("_")[0]);
                if (context.getServer().getLanguage().equals(languageCode)) {
                    sendSyntaxError(context, "core_language-already-using", locale.getDisplayName(locale));
                    return;
                }
                context.getServer().setLanguage(languageCode);
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(LupoColor.GREEN.getColor());
                builder.setAuthor(LupoBot.getInstance().getSelfUser().getName() + " (" + context.getGuild().getId() + ")", null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
                builder.setDescription(context.getServer().translate(null, "core_language-changed", locale.getDisplayName(locale)));
                builder.setTimestamp(context.getMessage().getTimeCreated());
                send(context, builder);
            } else {
                sendSyntaxError(context, "core_language-not-exists");
            }
        } else {
            ArrayList<Page> pages = new ArrayList<>();

            // Page with all languages listed
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.YELLOW.getColor());
            builder.setTitle(context.getServer().translate(null, "core_available-languages"));
            for (String language : LupoBot.getInstance().getAvailableLanguages()) {
                Locale locale = new Locale(language.split("_")[0]);
                String name = locale.getDisplayName(locale);
                String flag = ":flag_" + language.split("_")[1].toLowerCase() + ":";
                builder.addField(flag + " " + name, language, false);
            }
            Page list = new Page(builder.build());
            list.getWhitelist().add(context.getUser().getId());
            pages.add(list);

            // Page with help
            Page help = new Page(getHelpBuilder(context).build());
            help.getWhitelist().add(context.getUser().getId());
            pages.add(help);

            Paginator.paginate(context, pages, 120);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }

    private String getLanguageCodeByName(String name) {
        for (String code : LupoBot.getInstance().getAvailableLanguages()) {
            Locale locale = new Locale(code.split("_")[0]);
            if (name.equalsIgnoreCase(locale.getDisplayName(locale))) {
                return code;
            }
        }
        return null;
    }
}
