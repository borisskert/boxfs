package de.borisskert.boxfs.attributes;

import java.nio.file.attribute.BasicFileAttributes;

public class Attributes {
    private Attributes() {
    }

    public static <A extends BasicFileAttributes> A directory() {
        @SuppressWarnings("unchecked")
        A attrs = (A) new BoxFsDirectoryAttributes();
        return attrs;
    }

    public static <A extends BasicFileAttributes> A file() {
        @SuppressWarnings("unchecked")
        A attrs = (A) new BoxFsFileAttributes();
        return attrs;
    }
}
