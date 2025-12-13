package de.borisskert.boxfs.filesystem.windows;

import de.borisskert.boxfs.BoxFs;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.FileSystem;

@DisplayName("Windows FileSystemTest (BoxFs)")
class BoxFsTest extends FileSystemTest {
    @Override
    FileSystem getFs() {
        return BoxFs.windows();
    }
}
