package com.redhat.devtools.intellij.telemetry.core.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MapBuilder {

    private final Map<String, Object> map = new HashMap<>();

    public MapBuilder pair(String key, String value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder pair(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder pairs(Collection<AbstractMap.SimpleEntry<String, Object>> entries) {
        if (entries == null) {
            return this;
        }
        entries.stream().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
        return this;
    }

    public MapValueBuilder mapPair(String key) {
        return new MapValueBuilder(key);
    }

    public Map<String, Object> build() {
        return map;
    }

    public class MapValueBuilder {
        private final Map<String, Object> map = new HashMap<>();
        private final String key;

        private MapValueBuilder(String key) {
            this.key = key;
        }

        public MapValueBuilder pair(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public MapValueBuilder pairs(Collection<AbstractMap.SimpleEntry<String, Object>> entries) {
            if (entries == null) {
                return this;
            }
            entries.stream().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
            return this;
        }

        public MapBuilder build() {
            MapBuilder.this.pair(key, map);
            return MapBuilder.this;
        }
    }
}
