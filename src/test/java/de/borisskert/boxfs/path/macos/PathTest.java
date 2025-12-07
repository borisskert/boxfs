package de.borisskert.boxfs.path.macos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

abstract class PathTest {
    abstract FileSystem getFs() throws IOException;

    private FileSystem defaultFs;
    private FileSystem fs;

    @BeforeEach
    void setup() throws IOException {
        defaultFs = FileSystems.getDefault();
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
            assertThat(path.isAbsolute()).isTrue();

            Path name0 = path.getName(0);
            assertThat(name0).isNotNull();
            assertThat(name0.toString()).isEqualTo("tmp");

            Path name1 = path.getName(1);
            assertThat(name1).isNotNull();
            assertThat(name1.toString()).isEqualTo("test");

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

    @Nested
    class GetNestedPath {
        Path path;

        @BeforeEach
        void setup() {
            path = fs.getPath("/tmp/a/b/c/d/test");
        }

        @Test
        void shouldCreateNestedPath() {
            assertThat(path).isNotNull();
            assertThat(path.toString()).isEqualTo("/tmp/a/b/c/d/test");
            assertThat(path.toAbsolutePath().toString()).isEqualTo("/tmp/a/b/c/d/test");
        }

        @Nested
        class GetParent {
            Path parentPath;

            @BeforeEach
            void setup() {
                parentPath = path.getParent();
            }

            @Test
            void shouldCreateParentPath() {
                assertThat(parentPath).isNotNull();
                assertThat(parentPath.toString()).isEqualTo("/tmp/a/b/c/d");
                assertThat(parentPath.toAbsolutePath().toString()).isEqualTo("/tmp/a/b/c/d");
            }

            @Nested
            class RelativizePath {
                Path relativizedPath;

                @BeforeEach
                void setup() {
                    relativizedPath = parentPath.relativize(path);
                    assertThat(parentPath.toAbsolutePath().toString()).isEqualTo("/tmp/a/b/c/d");
                }

                @Test
                void shouldCreateRelativizedPath() {
                    assertThat(relativizedPath).isNotNull();
                    assertThat(relativizedPath.toString()).isEqualTo("test");
                    assertThat(relativizedPath.getNameCount()).isEqualTo(1);
                    assertThat(relativizedPath.getFileName().toString()).isEqualTo("test");
                    assertThat(relativizedPath.isAbsolute()).isFalse();
                    assertThat(relativizedPath.getRoot()).isNull();
                    assertThat(relativizedPath.getParent()).isNull();

                    String absolute = relativizedPath.toAbsolutePath().toString();
                    assertThat(absolute).startsWith("/");
                    assertThat(absolute).endsWith("/test");
                }
            }
        }
    }
}
