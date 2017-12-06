/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.coffig.spi;

import static com.sun.jmx.mbeanserver.Util.cast;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.seedstack.coffig.internal.ConfigurationErrorCode;
import org.seedstack.coffig.internal.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseWatchingProvider implements ConfigurationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWatchingProvider.class);
    private final Set<Path> paths = new HashSet<>();
    private final WatchService watcher;
    private final WatchEvent.Modifier modifier;
    private final Map<WatchKey, Path> keys;

    protected BaseWatchingProvider() {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.modifier = determineModifier();
            this.keys = new HashMap<>();
        } catch (IOException e) {
            throw ConfigurationException.wrap(e, ConfigurationErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    protected void watchSource(URL url) {
        try {
            Path path = Paths.get(url.toURI());
            Path parent = path.getParent();
            if (!keys.containsValue(parent)) {
                keys.put(parent.register(watcher,
                        new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY},
                        modifier),
                        parent);
            }
            this.paths.add(path);
        } catch (Exception e) {
            LOGGER.warn("Unable to watch URL for changes: " + url.toExternalForm(), e);
        }
    }

    @Override
    public boolean watch() {
        WatchKey key;
        boolean hasChanges = false;
        while ((key = watcher.poll()) != null) {
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
                    if (paths.contains(path)) {
                        hasChanges = true;
                        fileChanged(path);
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
        return hasChanges;
    }

    protected abstract void fileChanged(Path path);

    private WatchEvent.Modifier determineModifier() {
        try {
            Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
            return SensitivityWatchEventModifier.HIGH;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
