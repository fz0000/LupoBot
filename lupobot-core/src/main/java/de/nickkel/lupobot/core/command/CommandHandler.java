package de.nickkel.lupobot.core.command;

import com.google.common.reflect.ClassPath;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.awt.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CommandHandler {

    public void runCommand(CommandContext context) {
        LupoServer server = LupoServer.getByGuild(context.getGuild());
        LupoUser user = LupoUser.getByMember(context.getMember());

        LupoCommand command = null;
        for (LupoCommand all : LupoBot.getInstance().getCommands()) {
            if (context.getLabel().equalsIgnoreCase(all.getInfo().name())) {
                command = all;
            }

            for (String alias : all.getInfo().aliases()) {
                if (context.getLabel().equalsIgnoreCase(alias)) {
                    command = all;
                }
            }
        }

        if (command == null) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(server.translate(null, "core_command-error"));
            builder.setDescription(server.translate(null, "core_command-not-found"));
            builder.setColor(LupoColor.DARK_GRAY.getColor());
            builder.setFooter(server.translate(null, "core_tried-command", server.getPrefix() + context.getLabel()));
            context.getChannel().sendMessage(builder.build()).queue();
            return;
        }

        LupoPlugin plugin = null;
        for(LupoPlugin all : LupoBot.getInstance().getPlugins()) {
            if(all.getCommands().contains(command)) {
                plugin = all;
            }
        }
        context.setPlugin(plugin);

        for(Permission permission : command.getInfo().permissions()) {
            if(!context.getMember().getPermissions().contains(permission)) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(server.translate(null, "core_command-error"));
                builder.setDescription(server.translate(null, "core_command-no-user-permission"));
                builder.addField(server.translate(null, "core_command-permission"), permission.toString(), false);
                builder.setColor(LupoColor.DARK_GRAY.getColor());
                builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
                context.getChannel().sendMessage(builder.build()).queue();
                return;
            }
        }
        if(plugin != null && !server.getPlugins().contains(plugin)) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(server.translate(null, "core_command-error"));
            builder.setDescription(server.translate(null, "core_command-no-plugin"));
            builder.addField(server.translate(null, "core_command-plugin"), server.translatePluginName(plugin), false);
            builder.setColor(LupoColor.DARK_GRAY.getColor());
            builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
            context.getChannel().sendMessage(builder.build()).queue();
            return;
        }

        if(user.getCooldowns().containsKey(command)) {
            long leftCooldown = (user.getCooldowns().get(command)+command.getInfo().cooldown()*1000L)-System.currentTimeMillis();
            String time = String.format("%d " + server.translate(null, "core_command-cooldown-minutes") + ", %d " + server.translate(null, "core_command-cooldown-seconds"),
                    TimeUnit.MILLISECONDS.toMinutes(leftCooldown),
                    TimeUnit.MILLISECONDS.toSeconds(leftCooldown) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(leftCooldown))
            );
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.DARK_GRAY.getColor());
            builder.setAuthor(context.getMember().getUser().getAsTag(), null, context.getMember().getUser().getAvatarUrl());
            builder.setDescription(context.getServer().translate(null, "core_command-cooldown", time));
            builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
            context.getChannel().sendMessage(builder.build()).queue();
            return;
        }

        try {
            command.onCommand(context);
            if(command.getInfo().cooldown() != 0) {
                user.getCooldowns().put(command, System.currentTimeMillis());
                LupoCommand finalCommand = command;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        user.getCooldowns().remove(finalCommand);
                    }
                }, command.getInfo().cooldown()*1000L);
            }

        } catch(PermissionException permissionException) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(server.translate(null, "core_command-error"));
            builder.setDescription(server.translate(null, "core_command-no-bot-permission"));
            builder.addField(server.translate(null, "core_command-permission"), permissionException.getPermission().toString(), false);
            builder.setColor(LupoColor.DARK_GRAY.getColor());
            builder.setFooter(server.translate(null, server.getPrefix() + "core_used-command", server.getPrefix() + context.getLabel()));
            context.getChannel().sendMessage(builder.build()).queue();
        } catch(Exception e) {
            String stackTrace = "";
            for(StackTraceElement element : e.getStackTrace()) {
                stackTrace = stackTrace + "\n" + element.toString();
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(server.translate(null, "core_command-error-report", LupoBot.getInstance().getConfig().getString("supportServerUrl")));
            builder.setDescription("StackTrace: ```" + stackTrace + "```");
            builder.setColor(LupoColor.RED.getColor());
            builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
            context.getChannel().sendMessage(builder.build()).queue();
            e.printStackTrace();
        }

    }

    private boolean existsCommand(LupoCommand command) {
        return LupoBot.getInstance().getCommands().contains(command);
    }

    public void registerCommand(LupoPlugin plugin, LupoCommand command) {
        if (!existsCommand(command)) {
            LupoBot.getInstance().getCommands().add(command);
            if(plugin != null) {
                plugin.getCommands().add(command);
            }
            LupoBot.getInstance().getLogger().info("Registered command " + command.getInfo().name());
        }
    }

    public void unregisterCommand(LupoCommand command) {
        if (existsCommand(command)) {
            LupoBot.getInstance().getCommands().remove(command);
            LupoBot.getInstance().getLogger().info("Unregistered command " + command.getInfo().name());
        }
    }

    public void registerCommands(ClassLoader loader, String packageName) { // only for the core
        ClassPath classPath = null;
        try {
            classPath = ClassPath.from(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith(packageName)) {
                    final Class<?> clazz = info.load();
                    Object object = clazz.newInstance();
                    if (!clazz.isAnnotationPresent(CommandInfo.class) && object instanceof LupoCommand) {
                        throw new IllegalArgumentException("Command " + clazz.getClass().getSimpleName() + " is missing the @CommandInfo annotation");
                    } else {
                        LupoCommand command = (LupoCommand) object;
                        registerCommand(null, command);
                    }
                }
            }
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void registerCommands(LupoPlugin plugin, String packageName) { // only for plugins
        ClassPath classPath = null;
        try {
            classPath = ClassPath.from(plugin.getClass().getClassLoader());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(plugin.getClass().getClassLoader()).getTopLevelClasses()) {
                if (info.getName().startsWith(packageName)) {
                    final Class<?> clazz = info.load();
                    Object object = clazz.newInstance();
                    if (!clazz.isAnnotationPresent(CommandInfo.class) && object instanceof LupoCommand) {
                        throw new IllegalArgumentException("Command " + clazz.getClass().getSimpleName() + " is missing the @CommandInfo annotation");
                    } else {
                        LupoCommand command = (LupoCommand) object;
                        registerCommand(plugin, command);
                    }
                }
            }
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
