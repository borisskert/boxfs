package de.borisskert.boxfs.filesystem;

import de.borisskert.boxfs.decorator.CloseableFileSystem;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class DefaultTest extends FileSystemTest {
    @Override
    FileSystem getFs() {
        return CloseableFileSystem.decorate(FileSystems.getDefault());
    }
}
