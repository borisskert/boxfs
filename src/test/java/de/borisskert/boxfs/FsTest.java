package de.borisskert.boxfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

abstract class FsTest {

    abstract FileSystem getFs();

    private FileSystem fs;

    @Nested
    class GivenBoxFs {
        @BeforeEach
        void setup() {
            fs = getFs();
        }

        @Test
        void shouldReturnOpenFileSystem() {
            assertThat(fs).isNotNull();
            assertThat(fs.isOpen()).isTrue();
            assertThat(fs.getRootDirectories()).isNotNull();
        }

        @Test
        void shouldCloseFileSystem() throws Exception {
            fs.close();
            assertThat(fs.isOpen()).isFalse();
        }
    }
}
