package de.nickkel.lupobot.core.command;

import com.google.common.reflect.ClassPath;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.language.LanguageHandler;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CommandHandler {

    private int commandThreadNumber = 0;
    private final ExecutorService commandService = this.createCommandPool(r -> {
       Thread thread = new Thread(r);
       thread.setDaemon(false);
       thread.setUncaughtExceptionHandler((t, e) -> LupoBot.getInstance().getLogger().error("An unexpected exception occurred in " + t.getName() + ":", e));
       thread.setName("Command Thread #" + commandThreadNumber++);
       return thread;
    });

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
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
            builder.setDescription(server.translate(null, "core_command-not-found"));
            builder.setColor(LupoColor.DARK_GRAY.getColor());
            builder.setFooter(server.translate(null, "core_tried-command", server.getPrefix() + context.getLabel()));
            context.getChannel().sendMessage(builder.build()).queue();
            return;
        }
        context.setCommand(command);

        LupoPlugin plugin = null;
        for (LupoPlugin all : LupoBot.getInstance().getPlugins()) {
            if (all.getCommands().contains(command)) {
                plugin = all;
            }
        }
        context.setPlugin(plugin);

        context.setEphemeral(server.isSlashInvisible());
        for (Permission permission : command.getInfo().permissions()) {
            if (!context.getMember().getPermissions().contains(permission)) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
                builder.setDescription(server.translate(null, "core_command-no-user-permission"));
                builder.addField(server.translate(null, "core_command-permission"), permission.toString(), false);
                builder.setColor(LupoColor.DARK_GRAY.getColor());
                builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
                command.send(context, builder);
                return;
            }
        }
        if (plugin != null && !server.getPlugins().contains(plugin)) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
            builder.setDescription(server.translate(null, "core_command-no-plugin", plugin.getInfo().name()));
            builder.addField(server.translate(null, "core_command-plugin"), server.translatePluginName(plugin), false);
            builder.setColor(LupoColor.DARK_GRAY.getColor());
            builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
            command.send(context, builder);
            return;
        }

        if (user.getCooldowns().containsKey(command)) {
            long leftCooldown = user.getCooldowns().get(command)+command.getInfo().cooldown()*1000L-System.currentTimeMillis();
            if (leftCooldown > 0) {
                String time = String.format("%d " + server.translate(null, "core_minutes") + ", %d " + server.translate(null, "core_seconds"),
                        TimeUnit.MILLISECONDS.toMinutes(leftCooldown),
                        TimeUnit.MILLISECONDS.toSeconds(leftCooldown) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(leftCooldown))
                );
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(LupoColor.DARK_GRAY.getColor());
                builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
                builder.setDescription(context.getServer().translate(null, "core_command-cooldown", time));
                builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
                command.send(context, builder);
                return;
            } else {
                user.getCooldowns().remove(command);
            }
        }

        if (command.getInfo().staffPower() != -1) {
            boolean error = user.getStaffGroup().getPower() <= command.getInfo().staffPower();
            if (error) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
                builder.setDescription(server.translate(null, "core_command-no-user-permission"));
                builder.addField(server.translate(null, "core_command-staff-power-set"), server.formatLong(user.getStaffGroup().getPower()), false);
                builder.addField(server.translate(null, "core_command-staff-power-needed"), command.getInfo().name(), false);
                builder.setColor(LupoColor.DARK_GRAY.getColor());
                builder.setFooter(server.translate(null,  "core_used-command", server.getPrefix() + context.getLabel()));
                command.send(context, builder);
                return;
            }
        }

        try {
            if (command.isDisabled()) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
                builder.setDescription(server.translate(null, "core_command-disabled"));
                builder.setColor(LupoColor.RED.getColor());
                builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
                command.send(context, builder,
                        Button.link(LupoBot.getInstance().getConfig().getString("supportServerUrl"), context.getServer().translate(context.getPlugin(), "core_command-disabled-link-support"))
                );
                return;
            }

            LupoCommand finalCommand = command;
            this.commandService.execute(() -> {
                if (context.getSlash() != null) {
                    finalCommand.onSlashCommand(context, context.getSlash());
                } else {
                    finalCommand.onCommand(context);
                }
                if (finalCommand.getInfo().cooldown() != 0) {
                    user.getCooldowns().put(finalCommand, System.currentTimeMillis());
                }
            });
        } catch (PermissionException permissionException) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null, context.getMember().getUser().getAvatarUrl());
            builder.setDescription(server.translate(null, "core_command-no-bot-permission"));
            builder.addField(server.translate(null, "core_command-permission"), permissionException.getPermission().toString(), false);
            builder.setColor(LupoColor.DARK_GRAY.getColor());
            builder.setFooter(server.translate(null, server.getPrefix() + "core_used-command", server.getPrefix() + context.getLabel()));
            command.send(context, builder);
        } catch (Exception e) {
            e.printStackTrace();
            String stackTrace = "";
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace = stackTrace + "\n" + element.toString();
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(server.translate(null, "core_command-error-report"), LupoBot.getInstance().getConfig().getString("supportServerUrl"),
                    LupoBot.getInstance().getSelfUser().getAvatarUrl());
            builder.addField("Message:", e.getMessage() + " ", false);
            builder.addField("StackTrace:", "```" + stackTrace.substring(0, 1000) + "```", false);
            builder.setColor(LupoColor.RED.getColor());
            builder.setFooter(server.translate(null, "core_used-command", server.getPrefix() + context.getLabel()));
            command.send(context, builder);
        }
    }

    private boolean existsCommand(LupoCommand command) {
        return LupoBot.getInstance().getCommands().contains(command);
    }

    public void registerCommand(LupoPlugin plugin, LupoCommand command) {
        if (!existsCommand(command)) {
            LupoBot.getInstance().getCommands().add(command);
            if (plugin != null) {
                plugin.getCommands().add(command);
                command.setPlugin(plugin);
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

    public void registerSlashCommands() {
        LupoBot.getInstance().getSelfUser().getJDA().updateCommands().complete();
        CommandListUpdateAction commands = LupoBot.getInstance().getSelfUser().getJDA().updateCommands();

        List<CommandData> commandData = new ArrayList<>();
        for (LupoCommand command : LupoBot.getInstance().getCommands()) {
            if (!command.getInfo().hidden() || command.getInfo().staffPower() == -1) {
                LanguageHandler handler = LupoBot.getInstance().getLanguageHandler();
                String plugin = "core";
                if (command.getPlugin() != null) plugin = command.getPlugin().getInfo().name();
                if (command.getPlugin() != null) handler = command.getPlugin().getLanguageHandler();

                String description = handler.translate("en_US", plugin + "_" + command.getInfo().name() + "-description");
                if (description.length() > 100) description = description.substring(0, 100);

                CommandData data = new CommandData(command.getInfo().name(), description);
                for (SlashOption option : command.getSlashOptions()) {
                    String optionDesc = handler.translate("en_US", plugin + "_" + command.getInfo().name() + "-option-" + option.name());
                    if (optionDesc.length() > 100) optionDesc = optionDesc.substring(0, 100);
                    OptionData optionData = new OptionData(option.type(), option.name(), optionDesc, option.required());
                    for (String choice : option.choices()) {
                        optionData.addChoice(choice, choice);
                    }
                    data.addOptions(optionData);
                }
                for (SlashSubCommand subCommand : command.getSlashSubCommands()) {
                    String subDesc = handler.translate("en_US", plugin + "_" + command.getInfo().name() + "-sub-" + subCommand.name());
                    if (subDesc.length() > 100) subDesc = subDesc.substring(0, 100);
                    SubcommandData subData = new SubcommandData(subCommand.name(), subDesc);
                    for (SlashOption option : subCommand.options()) {
                        String subOptionDesc = handler.translate("en_US", plugin + "_" + command.getInfo().name() + "-sub-" + subCommand.name() + "-option-" + option.name());
                        if (subOptionDesc.length() > 100) subOptionDesc = subOptionDesc.substring(0, 100);
                        subData.addOption(option.type(), option.name(), subOptionDesc, option.required());
                    }
                    data.addSubcommands(subData);
                }
                commandData.add(data);
            }
        }
        commands.addCommands(commandData).queue();
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

    public void registerCommands (LupoPlugin plugin, String packageName) { // only for plugins
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

    public ExecutorService createCommandPool(ThreadFactory factory) {
        return new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 4, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), factory);
    }
}
