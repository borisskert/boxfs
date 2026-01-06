package de.borisskert.boxfs.filesystem.windows;

import de.borisskert.boxfs.BoxFs;
import de.borisskert.boxfs.windows.BoxFsTestingBasicFileAttributes;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.FileSystem;
import java.nio.file.attribute.BasicFileAttributes;

@DisplayName("Windows FileSystemTest (BoxFs)")
class BoxFsTest extends FileSystemTest {
    @Override
    FileSystem getFs() {
        return BoxFs.windows();
    }

    @Override
    Class<? extends BasicFileAttributes> fileAttributesClassForDebug() {
        return BoxFsTestingBasicFileAttributes.class;
    }
}
