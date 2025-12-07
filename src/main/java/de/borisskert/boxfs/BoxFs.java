package de.borisskert.boxfs;

import java.nio.file.FileSystem;

public class BoxFs {
    private BoxFs() {
    }

    public static FileSystem windows() {
        return de.borisskert.boxfs.windows.BoxFsFileSystem.windows();
    }

    public static FileSystem macos() {
        return de.borisskert.boxfs.macos.BoxFsFileSystem.macos();
    }
}
