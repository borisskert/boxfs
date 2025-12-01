package de.borisskert.boxfs.path;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class DefaultTest extends PathTest {
    @Override
    FileSystem getFs() {
        return FileSystems.getDefault();
    }
}
