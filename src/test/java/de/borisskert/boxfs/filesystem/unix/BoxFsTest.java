package de.borisskert.boxfs.filesystem.unix;

import de.borisskert.boxfs.BoxFs;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.FileSystem;

@DisplayName("Unix FileSystemTest (BoxFs)")
class BoxFsTest extends FileSystemTest {
    @Override
    FileSystem getFs() {
        return BoxFs.unix();
    }
}
