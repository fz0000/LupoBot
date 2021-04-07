package de.nickkel.lupobot.core.plugin;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.language.LanguageHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class PluginLoader {

    public PluginLoader() {
        List<Path> pluginPaths = new ArrayList<>();
        for (File file : new File("plugins").listFiles()) {
            if (file.getName().endsWith(".jar")) {
                LupoBot.getInstance().getLogger().info("Found plugin " + file.getName().replace(".jar", "") + "! Trying to load it ...");
                pluginPaths.add(file.toPath());
            }
        }

        if (pluginPaths.size() == 0) {
            LupoBot.getInstance().getLogger().warn("Could not found any plugin!");
        }

        for (Path path : pluginPaths) {
            loadPlugin(path);
        }
    }

    public void loadPlugin(Path path) {
        if (path == null) {
            throw new NullPointerException("Could not find plugin jar path!");
        }

        try (JarFile jarFile = new JarFile(path.toFile())) {
            String mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
            Class resourcesClass = new URLClassLoader(new URL[]{path.toFile().toURI().toURL()}).loadClass(mainClass);
            Class clazz = new URLClassLoader(new URL[]{path.toFile().toURI().toURL()}, this.getClass().getClassLoader()).loadClass(mainClass);
            if (!clazz.isAnnotationPresent(PluginInfo.class)) {
                LupoBot.getInstance().getLogger().error("Failed to load plugin " + path + " due to missing PluginInfo annotation!");
                return;
            }
            Object object = clazz.newInstance();
            if (!(object instanceof LupoPlugin)) {
                LupoBot.getInstance().getLogger().error("Failed to load plugin " + path + " due it is not an instance of LupoPlugin!");
                return;
            }

            LupoPlugin plugin = (LupoPlugin) object;
            plugin.setPath(path);
            plugin.setLanguageHandler(new LanguageHandler(resourcesClass));
            plugin.setResourcesClass(resourcesClass);
            plugin.loadResources();
            LupoBot.getInstance().getPlugins().add(plugin);
            LupoBot.getInstance().getLogger().info("Loaded plugin " + plugin.getInfo().name() + " version " + plugin.getInfo().version() + " by " + plugin.getInfo().author());

            if (!plugin.isEnabled()) {
                plugin.onEnable();
                plugin.setEnabled(true);
                LupoBot.getInstance().getLogger().info("Enabled plugin " + plugin.getInfo().name() + " version " + plugin.getInfo().version());
            }

        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException("Failed to load plugin " + path.toFile().getName() + "!", e);
        }
    }

    public void unloadPlugin(LupoPlugin plugin) {
        if (plugin.isEnabled()) {
            for (ListenerAdapter listener : plugin.getListeners()) {
                LupoBot.getInstance().getShardManager().removeEventListener(listener);
            }
            LupoBot.getInstance().getCommands().removeAll(plugin.getCommands());
            plugin.onDisable();
        }
        LupoBot.getInstance().getLogger().info("Unloaded plugin " + plugin.getInfo().name() + " version " + plugin.getInfo().version());
        LupoBot.getInstance().getPlugins().remove(plugin);
    }

    public void reloadPlugin(LupoPlugin plugin) {
        LupoBot.getInstance().getLogger().info("Reloading plugin " + plugin.getInfo().name() + " ...");
        Path path = plugin.getPath();
        unloadPlugin(plugin);
        loadPlugin(path);
    }
}
