package de.borisskert.boxfs;

import java.nio.file.FileSystem;

public class BoxFs {
    private BoxFs() {
    }

    public static FileSystem windows() {
        return new BoxFsFileSystem();
    }

    public static FileSystem macos() {
        return new BoxFsFileSystem();
    }
}
