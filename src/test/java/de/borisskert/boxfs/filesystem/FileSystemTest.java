package de.borisskert.boxfs.filesystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class FileSystemTest {

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

        @Nested
        class SimpleFileTests {
            String testFilePath = "/tmp/testfile.txt";
            Path file;

            @BeforeEach
            void setup() throws IOException {
                file = fs.getPath(testFilePath);
            }

            @Test
            void shouldNotExist() throws Exception {
                assertThat(Files.exists(file)).isFalse();
                assertThat(Files.isDirectory(file)).isFalse();
                assertThat(Files.notExists(file)).isTrue();
                assertThat(Files.isRegularFile(file)).isFalse();
                assertThat(Files.isHidden(file)).isFalse();
                assertThat(Files.isSymbolicLink(file)).isFalse();
                assertThat(Files.isReadable(file)).isFalse();
                assertThat(Files.isWritable(file)).isFalse();
                assertThat(Files.isExecutable(file)).isFalse();
                assertThatThrownBy(() -> Files.size(file)).isInstanceOf(IOException.class);
                assertThatThrownBy(() -> Files.readAttributes(file, "*")).isInstanceOf(IOException.class);
                assertThatThrownBy(() -> Files.getLastModifiedTime(file)).isInstanceOf(IOException.class);
                assertThat(Files.isSameFile(file, file)).isTrue();
            }

            @Nested
            class CreateFile {
                @BeforeEach
                void setup() throws IOException {
                    Files.createFile(file);
                }

                @AfterEach
                void teardown() throws IOException {
                    Files.deleteIfExists(file);
                }

                @Test
                void shouldCreateFile() throws Exception {
                    assertThat(Files.exists(file)).isTrue();
                    assertThat(Files.isDirectory(file)).isFalse();
                    assertThat(Files.notExists(file)).isFalse();
                    assertThat(Files.isRegularFile(file)).isTrue();
                    assertThat(Files.isHidden(file)).isFalse();
                    assertThat(Files.isSymbolicLink(file)).isFalse();
                    assertThat(Files.isReadable(file)).isTrue();
                    assertThat(Files.isWritable(file)).isTrue();
                    assertThat(Files.isExecutable(file)).isFalse();
                    assertThat(Files.size(file)).isEqualTo(0);
                    assertThat(Files.isSameFile(file, file)).isTrue();
                }
            }
        }

        @Nested
        class SimpleDirectoryTests {
            String testDirPath = "/tmp/testdir";
            Path dir;

            @BeforeEach
            void setup() throws IOException {
                dir = fs.getPath(testDirPath);
            }

            @Test
            void shouldNotExist() throws Exception {
                assertThat(Files.exists(dir)).isFalse();
                assertThat(Files.isDirectory(dir)).isFalse();
                assertThat(Files.notExists(dir)).isTrue();
                assertThat(Files.isRegularFile(dir)).isFalse();
                assertThat(Files.isHidden(dir)).isFalse();
                assertThat(Files.isSymbolicLink(dir)).isFalse();
                assertThat(Files.isReadable(dir)).isFalse();
                assertThat(Files.isWritable(dir)).isFalse();
                assertThat(Files.isExecutable(dir)).isFalse();
            }

            @Nested
            class CreateDirectory {

                @BeforeEach
                void setup() throws IOException {
                    Files.createDirectories(dir);
                }

                @AfterEach
                void teardown() throws IOException {
                    Files.deleteIfExists(dir);
                }

                @Test
                void shouldCreateDirectory() throws Exception {
                    assertThat(Files.exists(dir)).isTrue();
                    assertThat(Files.isDirectory(dir)).isTrue();
                    assertThat(Files.notExists(dir)).isFalse();
                    assertThat(Files.isRegularFile(dir)).isFalse();
                    assertThat(Files.isHidden(dir)).isFalse();
                    assertThat(Files.isSymbolicLink(dir)).isFalse();
                    assertThat(Files.isReadable(dir)).isTrue();
                    assertThat(Files.isWritable(dir)).isTrue();
                    assertThat(Files.isExecutable(dir)).isTrue();
                }

                @Nested
                class DeleteDirectory {
                    @BeforeEach
                    void setup() throws IOException {
                        Files.deleteIfExists(dir);
                    }

                    @Test
                    void shouldNotExist() throws IOException {
                        assertThat(Files.exists(dir)).isFalse();
                        assertThat(Files.isDirectory(dir)).isFalse();
                        assertThat(Files.notExists(dir)).isTrue();
                        assertThat(Files.isRegularFile(dir)).isFalse();
                        assertThat(Files.isHidden(dir)).isFalse();
                        assertThat(Files.isSymbolicLink(dir)).isFalse();
                        assertThat(Files.isReadable(dir)).isFalse();
                        assertThat(Files.isWritable(dir)).isFalse();
                        assertThat(Files.isExecutable(dir)).isFalse();
                    }

                    @Nested
                    class DeleteAgain {
                        @BeforeEach
                        void setup() throws IOException {
                            Files.deleteIfExists(dir);
                        }

                        @Test
                        void shouldNotExist() throws IOException {
                            assertThat(Files.exists(dir)).isFalse();
                            assertThat(Files.isDirectory(dir)).isFalse();
                            assertThat(Files.notExists(dir)).isTrue();
                            assertThat(Files.isRegularFile(dir)).isFalse();
                            assertThat(Files.isHidden(dir)).isFalse();
                            assertThat(Files.isSymbolicLink(dir)).isFalse();
                            assertThat(Files.isReadable(dir)).isFalse();
                            assertThat(Files.isWritable(dir)).isFalse();
                            assertThat(Files.isExecutable(dir)).isFalse();
                        }
                    }
                }
            }
        }

        @Nested
        class NestedDirectoryTests {
            String testDirPath = "/tmp/a/b/c/d/testdir";
            Path dir;

            @BeforeEach
            void setup() throws IOException {
                dir = fs.getPath(testDirPath);
            }

            @Test
            void shouldNotExist() throws Exception {
                assertThat(Files.exists(dir)).isFalse();
                assertThat(Files.isDirectory(dir)).isFalse();
                assertThat(Files.notExists(dir)).isTrue();
                assertThat(Files.isRegularFile(dir)).isFalse();
                assertThat(Files.isHidden(dir)).isFalse();
                assertThat(Files.isSymbolicLink(dir)).isFalse();
                assertThat(Files.isReadable(dir)).isFalse();
                assertThat(Files.isWritable(dir)).isFalse();
                assertThat(Files.isExecutable(dir)).isFalse();
            }

            @Nested
            class CreateDirectory {

                @BeforeEach
                void setup() throws IOException {
                    Files.createDirectories(dir);
                }

                @AfterEach
                void teardown() throws IOException {
                    Files.deleteIfExists(dir);
                }

                @Test
                void shouldCreateDirectory() throws Exception {
                    assertThat(Files.exists(dir)).isTrue();
                    assertThat(Files.isDirectory(dir)).isTrue();
                    assertThat(Files.notExists(dir)).isFalse();
                    assertThat(Files.isRegularFile(dir)).isFalse();
                    assertThat(Files.isHidden(dir)).isFalse();
                    assertThat(Files.isSymbolicLink(dir)).isFalse();
                    assertThat(Files.isReadable(dir)).isTrue();
                    assertThat(Files.isWritable(dir)).isTrue();
                    assertThat(Files.isExecutable(dir)).isTrue();
                }

                @Nested
                class GetParentDirectory {
                    String testDirPath = "/tmp/a/b/c";
                    Path parentDir;

                    @BeforeEach
                    void setup() {
                        parentDir = fs.getPath(testDirPath);
                    }

                    @Test
                    void shouldReturnParentDirectory() throws Exception {
                        assertThat(Files.exists(parentDir)).isTrue();
                        assertThat(Files.isDirectory(parentDir)).isTrue();
                        assertThat(Files.notExists(parentDir)).isFalse();
                        assertThat(Files.isRegularFile(parentDir)).isFalse();
                        assertThat(Files.isHidden(parentDir)).isFalse();
                        assertThat(Files.isSymbolicLink(parentDir)).isFalse();
                        assertThat(Files.isReadable(parentDir)).isTrue();
                        assertThat(Files.isWritable(parentDir)).isTrue();
                        assertThat(Files.isExecutable(parentDir)).isTrue();
                    }
                }

                @Nested
                class DeleteDirectory {
                    @BeforeEach
                    void setup() throws IOException {
                        Files.deleteIfExists(dir);
                    }

                    @Test
                    void shouldNotExist() throws IOException {
                        assertThat(Files.exists(dir)).isFalse();
                        assertThat(Files.isDirectory(dir)).isFalse();
                        assertThat(Files.notExists(dir)).isTrue();
                        assertThat(Files.isRegularFile(dir)).isFalse();
                        assertThat(Files.isHidden(dir)).isFalse();
                        assertThat(Files.isSymbolicLink(dir)).isFalse();
                        assertThat(Files.isReadable(dir)).isFalse();
                        assertThat(Files.isWritable(dir)).isFalse();
                        assertThat(Files.isExecutable(dir)).isFalse();
                    }

                    @Nested
                    class DeleteAgain {
                        @BeforeEach
                        void setup() throws IOException {
                            Files.deleteIfExists(dir);
                        }

                        @Test
                        void shouldNotExist() throws IOException {
                            assertThat(Files.exists(dir)).isFalse();
                            assertThat(Files.isDirectory(dir)).isFalse();
                            assertThat(Files.notExists(dir)).isTrue();
                            assertThat(Files.isRegularFile(dir)).isFalse();
                            assertThat(Files.isHidden(dir)).isFalse();
                            assertThat(Files.isSymbolicLink(dir)).isFalse();
                            assertThat(Files.isReadable(dir)).isFalse();
                            assertThat(Files.isWritable(dir)).isFalse();
                            assertThat(Files.isExecutable(dir)).isFalse();
                        }
                    }
                }
            }
        }
    }
}
