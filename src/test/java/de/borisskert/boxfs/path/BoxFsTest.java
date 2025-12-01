package de.borisskert.boxfs.path;

import de.borisskert.boxfs.BoxFs;

import java.nio.file.FileSystem;

public class BoxFsTest extends PathTest {
    @Override
    FileSystem getFs() {
        return BoxFs.create();
    }
}
