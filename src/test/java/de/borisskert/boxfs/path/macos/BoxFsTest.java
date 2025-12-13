package de.borisskert.boxfs.path.macos;

import de.borisskert.boxfs.BoxFs;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.FileSystem;

@DisplayName("MacOS PathTest (BoxFs)")
class BoxFsTest extends PathTest {
    @Override
    FileSystem getFs() {
        return BoxFs.macos();
    }
}
