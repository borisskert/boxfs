package de.borisskert.boxfs.filesystem.macos;

import de.borisskert.boxfs.wrapped.macos.WrappedFileSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.FileSystem;

@EnabledOnOs(OS.MAC)
@DisplayName("MacOS FileSystemTest (MacOS Wrapped FileSystem)")
class DefaultTest extends FileSystemTest {
    @Override
    FileSystem getFs() throws IOException {
        return WrappedFileSystem.create("/tmp/boxfs-test");
    }
}
