package de.nickkel.lupobot.core.internal.commands;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.core.util.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@CommandInfo(name = "exportdata", category = "core")
public class ExportDataCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        long lastDataExport = context.getUser().getData().getLong("lastDataExport");
        if (lastDataExport != -1 && lastDataExport+7776000000L-System.currentTimeMillis() > 0) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.RED.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                    context.getMember().getUser().getAvatarUrl());
            builder.setTimestamp(context.getMessage().getTimeCreated());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "core_exportdata-already-requested",
                    TimeUtils.format(context, lastDataExport+7776000000L-System.currentTimeMillis())));
            context.getChannel().sendMessage(builder.build()).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(LupoColor.ORANGE.getColor());
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                context.getMember().getUser().getAvatarUrl());
        builder.setTimestamp(context.getMessage().getTimeCreated());
        builder.setDescription(context.getServer().translate(context.getPlugin(), "core_exportdata-description"));

        builder.addField(context.getServer().translate(context.getPlugin(), "core_exportdata-user"),
                context.getServer().translate(context.getPlugin(), "core_exportdata-user-description"), false);
        if (context.getMember().isOwner()) {
            builder.addField(context.getServer().translate(context.getPlugin(), "core_exportdata-guild", ":mailbox_with_mail:"),
                    context.getServer().translate(context.getPlugin(), "core_exportdata-guild-owner"), false);
        } else {
            builder.addField(context.getServer().translate(context.getPlugin(), "core_exportdata-guild", ":mailbox_closed:"),
                    context.getServer().translate(context.getPlugin(), "core_exportdata-guild-not-owner"), false);
        }
        builder.addField(context.getServer().translate(context.getPlugin(), "core_exportdata-warning"),
                context.getServer().translate(context.getPlugin(), "core_exportdata-warning-description"), false);

        context.getChannel().sendMessage(builder.build()).queue();

        context.getMember().getUser().openPrivateChannel().queue((channel) -> {
            EmbedBuilder dataBuilder = new EmbedBuilder();
            dataBuilder.setColor(LupoColor.ORANGE.getColor());
            dataBuilder.setAuthor(context.getServer().translate(context.getPlugin(), "core_exportdata-request"), null,
                    LupoBot.getInstance().getSelfUser().getAvatarUrl());
            dataBuilder.setDescription(context.getServer().translate(context.getPlugin(), "core_exportdata-request-description"));
            dataBuilder.setTimestamp(context.getMessage().getTimeCreated());

            String userContent = context.getUser().getData().toJson();
            String serverContent = context.getServer().getData().toJson();
            File userFile = new File(context.getUser().getId() + ".txt");
            File serverFile = new File(context.getGuild().getId() + ".txt");

            try {
                FileWriter userWriter, serverWriter;

                userWriter = new FileWriter(userFile.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(userWriter);
                bw.write(userContent);
                bw.close();

                serverWriter = new FileWriter(serverFile.getAbsoluteFile());
                bw = new BufferedWriter(serverWriter);
                bw.write(serverContent);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (context.getMember().isOwner()) {
                channel.sendMessage(dataBuilder.build()).addFile(userFile).addFile(serverFile).queue();
                userFile.deleteOnExit();
            } else {
                channel.sendMessage(dataBuilder.build()).addFile(userFile).queue();
                serverFile.deleteOnExit();
            }
            context.getUser().getData().append("lastDataExport", System.currentTimeMillis());
        });
    }
}
