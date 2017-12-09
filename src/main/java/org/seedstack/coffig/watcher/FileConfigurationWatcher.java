/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.watcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.seedstack.shed.reflect.Classes.cast;

import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.seedstack.coffig.spi.ConfigurationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigurationWatcher implements ConfigurationWatcher, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileConfigurationWatcher.class);
    private static final WatchEvent.Modifier MODIFIER;
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final Map<Path, Set<Consumer<Path>>> listeners = new HashMap<>();
    private Thread watchThread;
    private boolean stop;

    static {
        SensitivityWatchEventModifier detectedModifier;
        try {
            Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
            detectedModifier = SensitivityWatchEventModifier.HIGH;
        } catch (ClassNotFoundException e) {
            detectedModifier = null;
        }
        MODIFIER = detectedModifier;
    }

    public static FileConfigurationWatcher getInstance() {
        return Holder.INSTANCE;
    }

    private FileConfigurationWatcher() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw ConfigurationException.wrap(e, ConfigurationErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    @Override
    public void startWatching() {
        if (watchThread == null) {
            watchThread = new Thread(this, "configFileWatcher");
            stop = false;
            watchThread.start();
        }
    }

    @Override
    public void stopWatching() {
        if (watchThread != null) {
            stop = true;
            watchThread.interrupt();
            try {
                watchThread.join(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public synchronized void watchFile(Path path, Consumer<Path> listener) {
        if (!path.toFile().isFile()) {
            throw new IllegalArgumentException("Path " + path.toString() + " doesn't reference a file");
        }

        try {
            Path parent = path.getParent();
            if (!keys.containsValue(parent)) {
                keys.put(parent.register(watcher,
                        new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY},
                        MODIFIER),
                        parent);
            }
            listeners.computeIfAbsent(path, key -> new HashSet<>()).add(listener);
            LOGGER.debug("Will watch configuration file " + path);
        } catch (Exception e) {
            LOGGER.warn("Unable setup watch for path: {}", path, e);
        }
    }

    @Override
    public void run() {
        while (!stop) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                break;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (kind != OVERFLOW) {
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path path = dir.resolve(name);
                    Set<Consumer<Path>> listeners = this.listeners.get(path);
                    LOGGER.info("Configuration file changed: " + path.toString());
                    if (listeners != null) {
                        listeners.forEach(listener -> listener.accept(path));
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private static class Holder {
        private static final FileConfigurationWatcher INSTANCE = new FileConfigurationWatcher();
    }
}
