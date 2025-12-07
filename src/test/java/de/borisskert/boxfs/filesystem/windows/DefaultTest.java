package de.borisskert.boxfs.filesystem.windows;

import de.borisskert.boxfs.wrapped.windows.WrappedFileSystem;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.FileSystem;

@EnabledOnOs(OS.WINDOWS)
public class DefaultTest extends FileSystemTest {
    @Override
    FileSystem getFs() throws IOException {
        return WrappedFileSystem.create("C:\\Temp\\boxfs-test");
    }
}
