package de.nickkel.lupobot.launcher;

import java.net.URL;
import java.net.URLClassLoader;

public final class LauncherClassLoader extends URLClassLoader {

     static {
         ClassLoader.registerAsParallelCapable();
     }

    public LauncherClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    public LauncherClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}