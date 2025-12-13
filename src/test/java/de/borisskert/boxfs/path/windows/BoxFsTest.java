package de.borisskert.boxfs.path.windows;

import de.borisskert.boxfs.BoxFs;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.FileSystem;

@DisplayName("Windows PathTest (BoxFs)")
class BoxFsTest extends PathTest {
    @Override
    FileSystem getFs() {
        return BoxFs.windows();
    }
}
