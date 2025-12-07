package de.borisskert.boxfs.windows;

import java.util.Objects;

class BoxFsBasicAttributesKey {
    public static final BoxFsBasicAttributesKey READONLY = new BoxFsBasicAttributesKey("dos:readonly");

    private final String name;
    private final String attribute;

    BoxFsBasicAttributesKey(String attribute) {
        this.attribute = Objects.requireNonNull(attribute);
        this.name = readName(attribute);
    }

    private static String readName(String attribute) {
        int pos = attribute.indexOf(':');
        if (pos == -1) {
            return attribute;
        } else {
            return attribute.substring(pos + 1);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BoxFsBasicAttributesKey that = (BoxFsBasicAttributesKey) o;
        return Objects.equals(attribute, that.attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attribute);
    }

    @Override
    public String toString() {
        return "BoxFsBasicAttributeKey{" +
                "name='" + name + '\'' +
                ", attribute='" + attribute + '\'' +
                '}';
    }

    public static BoxFsBasicAttributesKey of(String attributes) {
        return new BoxFsBasicAttributesKey(attributes);
    }
}
