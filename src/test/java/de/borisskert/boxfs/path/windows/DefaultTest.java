package de.borisskert.boxfs.path.windows;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@EnabledOnOs(OS.WINDOWS)
public class DefaultTest extends PathTest {
    @Override
    FileSystem getFs() throws IOException {
        return FileSystems.getDefault();
    }
}
