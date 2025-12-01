package de.borisskert.boxfs.filesystem;

import de.borisskert.boxfs.BoxFs;

import java.nio.file.FileSystem;

class BoxFsTest extends FileSystemTest {
    @Override
    FileSystem getFs() {
        return BoxFs.create();
    }
}
