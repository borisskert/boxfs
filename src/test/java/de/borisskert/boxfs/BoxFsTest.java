package de.borisskert.boxfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

class BoxFsTest {

    FileSystem fs;

    @Nested
    class GivenBoxFs {
        @BeforeEach
        void setup() {
            fs = BoxFs.create();
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
