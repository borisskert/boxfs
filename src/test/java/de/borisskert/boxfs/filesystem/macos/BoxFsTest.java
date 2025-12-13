package de.borisskert.boxfs.filesystem.macos;

import de.borisskert.boxfs.BoxFs;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.FileSystem;

@DisplayName("MacOS FileSystemTest (BoxFs)")
class BoxFsTest extends FileSystemTest {
    @Override
    FileSystem getFs() {
        return BoxFs.macos();
    }
}
