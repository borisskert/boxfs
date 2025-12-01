package de.borisskert.boxfs.path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

abstract class PathTest {
    abstract FileSystem getFs();

    private FileSystem fs;

    @BeforeEach
    void setup() {
        fs = getFs();
    }

    @Nested
    class GetSimplePath {
        Path path;

        @BeforeEach
        void setup() {
            path = fs.getPath("/tmp/test");
        }

        @Test
        void shouldCreateSimplePath() {
            assertThat(path).isNotNull();
            assertThat(path.toString()).isEqualTo("/tmp/test");
            assertThat(path.getFileName().toString()).isEqualTo("test");
            assertThat(path.getParent().toString()).isEqualTo("/tmp");
            assertThat(path.getRoot().toString()).isEqualTo("/");
            assertThat(path.getNameCount()).isEqualTo(2);
            assertThat(path.getName(0).toString()).isEqualTo("tmp");
            assertThat(path.getName(1).toString()).isEqualTo("test");
            assertThat(path.isAbsolute()).isTrue();
        }

        @Nested
        class GetAbsolutePath {
            Path absolutePath;

            @BeforeEach
            void setup() {
                absolutePath = path.toAbsolutePath();
            }

            @Test
            void shouldCreateAbsolutePath() {
                assertThat(absolutePath).isNotNull();
                assertThat(absolutePath.toString()).isEqualTo("/tmp/test");
            }
        }

        @Nested
        class GetSubPath {
            Path subPath;

            @BeforeEach
            void setup() {
                subPath = path.subpath(0, 2);
            }

            @Test
            void shouldCreateSubPath() {
                assertThat(subPath).isNotNull();
                assertThat(subPath.toString()).isEqualTo("tmp/test");
            }
        }

        @Nested
        class GetNormalizedPath {
            Path normalizedPath;

            @BeforeEach
            void setup() {
                normalizedPath = path.normalize();
            }

            @Test
            void shouldCreateNormalizedPath() {
                assertThat(normalizedPath).isNotNull();
                assertThat(normalizedPath.toString()).isEqualTo("/tmp/test");
            }
        }
    }
}
