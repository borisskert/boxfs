package de.borisskert.boxfs.filesystem.windows;

import de.borisskert.boxfs.wrapped.windows.WrappedFileSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.FileSystem;

@EnabledOnOs(OS.WINDOWS)
@DisplayName("Windows FileSystemTest (Windows Wrapped FileSystem)")
class DefaultTest extends FileSystemTest {
    @Override
    FileSystem getFs() throws IOException {
        return WrappedFileSystem.create("C:\\Temp\\boxfs-test");
    }
}
