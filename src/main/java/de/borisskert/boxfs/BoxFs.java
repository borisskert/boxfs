package de.borisskert.boxfs;

import java.nio.file.FileSystem;

public class BoxFs {
    private BoxFs() {
    }

    public static FileSystem create() {
        return new BoxFsFileSystem();
    }
}
