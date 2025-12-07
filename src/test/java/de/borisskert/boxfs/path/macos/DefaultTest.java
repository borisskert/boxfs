package de.borisskert.boxfs.path.macos;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@EnabledOnOs(OS.MAC)
public class DefaultTest extends PathTest {
    @Override
    FileSystem getFs() throws IOException {
        return FileSystems.getDefault();
    }
}
