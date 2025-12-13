package de.borisskert.boxfs.windows;

import java.util.Objects;

public class BoxFsFileName {
    private final String name;

    private BoxFsFileName(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BoxFsFileName boxFsFileName = (BoxFsFileName) o;
        return Objects.equals(toLowerCase(name), toLowerCase(boxFsFileName.name));
    }

    private static String toLowerCase(String name) {
        if (name == null) {
            return null;
        }

        return name.toLowerCase();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(toLowerCase(name));
    }

    public static BoxFsFileName of(String name) {
        return new BoxFsFileName(name);
    }
}
