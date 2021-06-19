package de.nickkel.lupobot.plugin.profile.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.profile.LupoProfilePlugin;
import de.nickkel.lupobot.plugin.profile.data.Profile;
import de.nickkel.lupobot.plugin.profile.enums.Gender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "setgender", category = "general")
@SlashOption(name = "gender", type = OptionType.STRING, choices = {"MALE", "FEMALE", "DIVERSE"})
public class SetGenderCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1) {
            String arg;
            if (context.getSlash() == null) {
                arg = context.getArgs()[0];
            } else {
                arg = context.getSlash().getOption("gender").getAsString();
            }

            Profile profile = LupoProfilePlugin.getInstance().getProfile(context.getMember());
            Gender gender = null;

            for (Gender all : Gender.values()) {
                if (all.toString().equalsIgnoreCase(arg)) {
                    gender = all;
                }
             }

            if (gender == null) {
                sendSyntaxError(context, "profile_setgender-invalid");
                return;
            }

            profile.setGender(gender);
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(LupoColor.GREEN.getColor())
                    .setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                            context.getMember().getUser().getAvatarUrl())
                    .setTimestamp(context.getTime())
                    .setDescription(context.getServer().translate(context.getPlugin(), "profile_setgender-success", gender.toString()));
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
