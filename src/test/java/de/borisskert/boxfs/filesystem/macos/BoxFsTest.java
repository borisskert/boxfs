package de.borisskert.boxfs.filesystem.macos;

import de.borisskert.boxfs.BoxFs;

import java.nio.file.FileSystem;

class BoxFsTest extends FileSystemTest {
    @Override
    FileSystem getFs() {
        return BoxFs.macos();
    }
}
