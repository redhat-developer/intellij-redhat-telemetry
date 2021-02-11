package com.redhat.devtools.intellij.telemetry.core.service.util;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder {

    private MapBuilder() {}

    public static MapBuilder instance() {
        return new MapBuilder();
    }

    private Map<String, Object> map = new HashMap<>();

    public MapBuilder pair(String key, String value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder pair(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public MapValueBuilder map(String key) {
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

        public MapBuilder build() {
            MapBuilder.this.pair(key, map);
            return MapBuilder.this;
        }
    }
}
