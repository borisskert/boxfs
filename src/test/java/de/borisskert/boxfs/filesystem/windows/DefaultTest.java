package de.borisskert.boxfs.filesystem.windows;

import de.borisskert.boxfs.wrapped.windows.WrappedBasicFileAttributes;
import de.borisskert.boxfs.wrapped.windows.WrappedFileSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

@EnabledOnOs(OS.WINDOWS)
@DisplayName("Windows FileSystemTest (Windows Wrapped FileSystem)")
class DefaultTest extends FileSystemTest {
    @Override
    FileSystem getFs() throws IOException {
        deleteRecursivelyIfExists(Paths.get("C:\\Temp\\boxfs-test"));
        return WrappedFileSystem.create("C:\\Temp\\boxfs-test");
    }

    @Override
    Class<? extends BasicFileAttributes> fileAttributesClassForDebug() {
        return WrappedBasicFileAttributes.class;
    }
}
