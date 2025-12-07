package de.borisskert.boxfs.windows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class BoxFsBasicAttributesMap {
    private final Map<BoxFsBasicAttributesKey, Object> attributesMap = new HashMap<>();

    public BoxFsBasicAttributesMap put(String key, Object value) {
        attributesMap.put(BoxFsBasicAttributesKey.of(key), value);
        return this;
    }

    public BoxFsBasicAttributesMap put(BoxFsBasicAttributesKey key, Object value) {
        attributesMap.put(key, value);
        return this;
    }

    public Map<String, Object> readAttributes(String attributes) {
        BoxFsBasicAttributesKey key = BoxFsBasicAttributesKey.of(attributes);

        return Optional.ofNullable(attributesMap.get(key)).map(
                v -> Collections.singletonMap(key.getName(), v)
        ).orElse(Collections.emptyMap());
    }

    public static BoxFsBasicAttributesMap empty() {
        return new BoxFsBasicAttributesMap();
    }

    public boolean isTrue(BoxFsBasicAttributesKey readonly) {
        return Optional.ofNullable(attributesMap.get(readonly))
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }
}
