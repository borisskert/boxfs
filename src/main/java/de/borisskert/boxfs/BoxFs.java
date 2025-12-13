package de.borisskert.boxfs;

import java.nio.file.FileSystem;

public class BoxFs {
    private BoxFs() {
    }

    public static FileSystem windows() {
        return de.borisskert.boxfs.windows.BoxFsFileSystem.create();
    }

    public static FileSystem macos() {
        return de.borisskert.boxfs.macos.BoxFsFileSystem.create();
    }

    public static FileSystem unix() {
        return de.borisskert.boxfs.unix.BoxFsFileSystem.create();
    }
}
