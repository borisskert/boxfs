package de.borisskert.boxfs;

import java.nio.file.FileSystem;

class BoxFsTest extends FsTest {
    @Override
    FileSystem getFs() {
        return BoxFs.create();
    }
}
