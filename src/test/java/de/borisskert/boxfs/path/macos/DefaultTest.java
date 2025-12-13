package de.borisskert.boxfs.path.macos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@EnabledOnOs(OS.MAC)
@DisplayName("MacOS PathTest (macOS Default FileSystem)")
class DefaultTest extends PathTest {
    @Override
    FileSystem getFs() throws IOException {
        return FileSystems.getDefault();
    }
}
