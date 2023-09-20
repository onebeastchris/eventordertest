package net.onebeastchris.extension.eventordertest;

import org.geysermc.event.Event;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.connection.GeyserBedrockPingEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class EventOrderTest implements Extension {

    @Subscribe
    public void onPreInit(GeyserPreInitializeEvent event) {
        logger().warning("Event fired: GeyserPreInitializeEvent");
        try {
            List<Class<?>> eventClasses = findEventClasses(findJarFilePath(Extension.class), "org/geysermc/geyser/api/event/", Event.class);
            logger().info("Found " + eventClasses.size() + " classes implementing Geyser's event interface");

            // remove spammy event
            eventClasses.remove(GeyserBedrockPingEvent.class); // spams console

            for (Class<?> clazz : eventClasses) {
                this.geyserApi().eventBus().subscribe(this, clazz.asSubclass(Event.class), (e) -> {
                    logger().warning("Event fired: " + clazz.getSimpleName());
                });
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<Class<?>> findEventClasses(String jarFilePath, String packagePath, Class<?> interfaceClass)
            throws ClassNotFoundException, IOException {
        if (jarFilePath == null) {
            throw new IllegalArgumentException("jarFilePath cannot be null");
        }

        List<Class<?>> eventClasses = new ArrayList<>();
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().startsWith(packagePath) && entry.getName().endsWith(".class")) {
                String className = entry.getName()
                        .replace("/", ".")
                        .substring(0, entry.getName().length() - 6);
                Class<?> clazz = Class.forName(className);

                if (interfaceClass.isAssignableFrom(clazz)) {
                    eventClasses.add(clazz);
                }
            }
        }
        jarFile.close();

        return eventClasses;
    }

    // find our Geyser jar file path
    public String findJarFilePath(Class<?> clazz) throws IOException {
        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();

        if (codeSource != null) {
            URL location = codeSource.getLocation();
            if (location != null) {
                String jarFilePath = location.getFile();
                return new File(jarFilePath).getCanonicalPath();
            }
        }
        return null;
    }
}
