package de.nickkel.lupobot.core.util;

import com.google.common.reflect.ClassPath;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import net.dv8tion.jda.api.hooks.EventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListenerRegister {

    private List<EventListener> listeners = new ArrayList<>();

    public ListenerRegister(LupoPlugin plugin, String packageName) {
        new ListenerRegister(plugin.getClass().getClassLoader(), packageName);
        plugin.setListeners(this.listeners);
    }

    @Deprecated // should only be used for the core, in no case for a plugin
    public ListenerRegister(ClassLoader loader, String packageName) {
        List<EventListener> listeners = new ArrayList<>();
        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith(packageName)) {
                    final Class<?> clazz = info.load();
                    Object object = clazz.newInstance();
                    EventListener listener = (EventListener) object;
                    LupoBot.getInstance().getShardManager().addEventListener(listener);
                    listeners.add(listener);
                    LupoBot.getInstance().getLogger().info("Registered listener " + listener.getClass().getSimpleName());
                }
            }
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        this.listeners = listeners;
    }
}