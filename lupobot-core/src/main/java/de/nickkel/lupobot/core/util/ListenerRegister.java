package de.nickkel.lupobot.core.util;

import com.google.common.reflect.ClassPath;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListenerRegister {

    private final List<ListenerAdapter> listeners = new ArrayList<>();


    @Deprecated // should only be used for the core, in no case for a plugin
    public ListenerRegister(ClassLoader loader, String packageName, LupoPlugin plugin) {
        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith(packageName)) {
                    final Class<?> clazz = info.load();
                    Object object = clazz.newInstance();
                    ListenerAdapter listener = (ListenerAdapter) object;
                    LupoBot.getInstance().getShardManager().addEventListener(listener);
                    if (plugin != null) {
                        plugin.getListeners().add(listener);
                    }
                    LupoBot.getInstance().getLogger().info("Registered listener " + listener.getClass().getSimpleName());
                }
            }
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}