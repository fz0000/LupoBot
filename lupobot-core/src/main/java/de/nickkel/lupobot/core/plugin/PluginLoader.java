package de.nickkel.lupobot.core.plugin;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.language.LanguageHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.jar.JarFile;

public class PluginLoader {

    private int pluginThreadNumber = 0;
    private final ExecutorService pluginService = this.createPluginPool(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(false);
        thread.setUncaughtExceptionHandler((t, e) -> LupoBot.getInstance().getLogger().error("An unexpected exception occurred in " + t.getName() + ":", e));
        thread.setName("Plugin Thread #" + pluginThreadNumber++);
        return thread;
    });

    public PluginLoader() {
        int plugins = 0;
        for (File file : new File("plugins").listFiles()) {
            if (file.getName().endsWith(".jar")) {
                LupoBot.getInstance().getLogger().info("Found plugin " + file.getName().replace(".jar", "") + "! Trying to load it ...");
                loadPlugin(file);
                plugins++;
            }
        }

        if (plugins == 0) {
            LupoBot.getInstance().getLogger().error("Could not find any plugins!");
        }
    }

    public void loadPlugin(File file) {
        LupoPlugin plugin = null;
        PluginHelper pluginHelper = new PluginHelper(file);
        URLClassLoader classLoader = pluginHelper.getPluginClassLoader();
        String main = null;
        try {
            main = new JarFile(file).getManifest().getMainAttributes().getValue("Main-Class");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Class<? extends LupoPlugin> mainClass;
        Class resourcesClass = null;
        try {
            mainClass = (Class<? extends LupoPlugin>) classLoader.loadClass(main);
            plugin = mainClass.newInstance();
            resourcesClass = new URLClassLoader(new URL[]{file.toURI().toURL()}).loadClass(main);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | MalformedURLException e) {
            e.printStackTrace();
        }

        if (plugin == null) {
            LupoBot.getInstance().getLogger().error("While plugin loading no class extending " + LupoPlugin.class.getSimpleName() + " was found in file " + file.getName() + "!");
            return;
        }

        plugin.setHelper(pluginHelper);
        plugin.setResourcesClass(resourcesClass);
        plugin.setLanguageHandler(new LanguageHandler(resourcesClass));
        plugin.setFile(file);
        plugin.loadResources();
        LupoBot.getInstance().getPlugins().add(plugin);
        LupoBot.getInstance().getLogger().info("Loaded plugin " + plugin.getInfo().name() + " version " + plugin.getInfo().version() + " by " + plugin.getInfo().author());

        if (!plugin.isEnabled()) {
            plugin.onEnable();
            plugin.setEnabled(true);
            LupoBot.getInstance().getLogger().info("Enabled plugin " + plugin.getInfo().name() + " version " + plugin.getInfo().version());
        }
    }

    public void unloadPlugin(LupoPlugin plugin) {
        if (plugin.isEnabled()) {
            Iterator<ListenerAdapter> listeners = plugin.getListeners().iterator();
            while (listeners.hasNext()) {
                ListenerAdapter listener = listeners.next();
                LupoBot.getInstance().getLogger().info("Unregistered listener " + listener.getClass().getSimpleName());
                LupoBot.getInstance().getShardManager().removeEventListener(listener);
            }

            Iterator<LupoCommand> commands = plugin.getCommands().iterator();
            while (commands.hasNext()) {
                LupoBot.getInstance().getCommandHandler().unregisterCommand(commands.next());
            }
            plugin.onDisable();
        }
        LupoBot.getInstance().getPlugins().removeIf(all -> all.getInfo().name().equalsIgnoreCase(plugin.getInfo().name()));
        LupoBot.getInstance().getLogger().info("Unloaded plugin " + plugin.getInfo().name() + " version " + plugin.getInfo().version());

        URLClassLoader classLoader = plugin.getHelper().getPluginClassLoader();
        try {
            classLoader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadPlugin(LupoPlugin plugin) {
        this.pluginService.execute(() -> {
            try {
                LupoBot.getInstance().getLogger().info("Reloading plugin " + plugin.getInfo().name() + " ...");
                unloadPlugin(plugin);
                Thread.sleep(1000L);
                loadPlugin(plugin.getFile());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public ExecutorService createPluginPool(ThreadFactory factory) {
        return new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 4, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), factory);
    }
}
