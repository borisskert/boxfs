package de.borisskert.boxfs.path.unix;

import de.borisskert.boxfs.BoxFs;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.FileSystem;

@DisplayName("Unix PathTest (BoxFs)")
class BoxFsTest extends PathTest {
    @Override
    FileSystem getFs() {
        return BoxFs.unix();
    }
}
