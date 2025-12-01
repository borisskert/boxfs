package de.borisskert.boxfs;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class DefaultFsTest extends FsTest {
    @Override
    FileSystem getFs() {
        return CloseableFileSystem.decorate(FileSystems.getDefault());
    }
}
