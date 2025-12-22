package de.borisskert.boxfs.filesystem.windows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class FileSystemTest {

    abstract FileSystem getFs() throws IOException;

    private FileSystem fs;

    @Nested
    class GivenFileSystem {
        @BeforeEach
        void setup() throws IOException {
            fs = getFs();
        }

        @AfterEach
        void teardown() throws IOException {
            fs.close();
        }

        @Test
        void shouldReturnOpenFileSystem() {
            assertThat(fs).isNotNull();
            assertThat(fs.isOpen()).isTrue();
            assertThat(fs.getRootDirectories()).isNotNull();
            assertThat(fs.isReadOnly()).isFalse();
        }

        @Nested
        class WhenGetRoot {
            Path root;

            @BeforeEach
            void setup() throws IOException {
                root = fs.getRootDirectories().iterator().next();
            }

            @Test
            void shouldReturnRootDirectory() throws IOException {
                assertThat(root).isNotNull();
                assertThat(root.toString()).isEqualTo("C:\\");
                assertThat(root.toAbsolutePath().toString()).isEqualTo("C:\\");
                assertThat(Files.exists(root)).isTrue();
                assertThat(Files.isDirectory(root)).isTrue();
                assertThat(Files.isRegularFile(root)).isFalse();
                assertThat(Files.isHidden(root)).isFalse();
                assertThat(Files.isSymbolicLink(root)).isFalse();
                assertThat(Files.isReadable(root)).isTrue();
                assertThat(Files.isWritable(root)).isTrue();
                assertThat(Files.isExecutable(root)).isTrue();
                assertThat(Files.isSameFile(root, root)).isTrue();
            }
        }

        @Nested
        class SimpleFileTests {
            String testFilePath = "C:\\testfile.txt";
            Path root;
            Path file;

            @BeforeEach
            void setup() throws IOException {
                root = fs.getPath("C:\\");
                file = fs.getPath(testFilePath);
            }

            @Test
            void shouldNotExist() throws Exception {
                assertThat(Files.exists(file)).isFalse();
                assertThat(Files.isDirectory(file)).isFalse();
                assertThat(Files.notExists(file)).isTrue();
                assertThat(Files.isRegularFile(file)).isFalse();
                assertThatThrownBy(() -> Files.isHidden(file)).isInstanceOf(IOException.class);
                assertThat(Files.isSymbolicLink(file)).isFalse();
                assertThat(Files.isReadable(file)).isFalse();
                assertThat(Files.isWritable(file)).isFalse();
                assertThat(Files.isExecutable(file)).isFalse();
                assertThatThrownBy(() -> Files.size(file)).isInstanceOf(IOException.class);
                assertThatThrownBy(() -> Files.readAttributes(file, "*")).isInstanceOf(IOException.class);
                assertThatThrownBy(() -> Files.getLastModifiedTime(file)).isInstanceOf(IOException.class);
                assertThat(Files.isSameFile(file, file)).isTrue();
                assertThat(file.toString()).isEqualTo(testFilePath);
            }

            @Nested
            class WriteToNonExistingFile {
                @AfterEach
                void teardown() throws IOException {
                    Files.deleteIfExists(file);
                }

                @Test
                void shouldCreateFileWhenWritingToNonExistingFile() throws IOException {
                    Files.write(file, "Hello World!".getBytes());

                    assertThat(Files.exists(file)).isTrue();
                    assertThat(Files.readAllBytes(file)).isEqualTo("Hello World!".getBytes());
                }
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
                    assertThat(Files.isExecutable(file)).isTrue();
                    assertThat(Files.size(file)).isEqualTo(0);
                    assertThat(Files.isSameFile(file, file)).isTrue();
                }

                @Test
                void shouldFailWhenTryingToCreateSameFileAgain() {
                    assertThatThrownBy(() -> Files.createFile(file))
                            .isInstanceOf(FileAlreadyExistsException.class);
                }

                @Test
                void shouldFailWhenTryingToCreateDirectoryWithSameNameAsFile() {
                    assertThatThrownBy(() -> Files.createDirectory(file))
                            .isInstanceOf(FileAlreadyExistsException.class);
                }

                @Test
                void shouldFailWhenTryingToCreateFileWithCreateNewOption() {
                    assertThatThrownBy(() -> Files.newByteChannel(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))
                            .isInstanceOf(FileAlreadyExistsException.class);
                }

                @Test
                void shouldFindTheFileInRootDirectory() throws IOException {
                    try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                        Iterator<Path> iterator = paths.iterator();

                        assertThat(iterator.hasNext()).isTrue();
                        assertThat(iterator.next()).isEqualTo(file);
                        assertThat(iterator.hasNext()).isFalse();
                    }
                }

                @Test
                void shouldFindFileInAnotherCase() throws IOException {
                    Path pathWithDifferentCase = fs.getPath(testFilePath.toUpperCase());

                    assertThat(Files.exists(pathWithDifferentCase)).isTrue();
                    assertThat(Files.isDirectory(pathWithDifferentCase)).isFalse();
                    assertThat(Files.notExists(pathWithDifferentCase)).isFalse();
                    assertThat(Files.isRegularFile(pathWithDifferentCase)).isTrue();
                    assertThat(Files.isHidden(pathWithDifferentCase)).isFalse();
                    assertThat(Files.isSymbolicLink(pathWithDifferentCase)).isFalse();
                    assertThat(Files.isReadable(pathWithDifferentCase)).isTrue();
                    assertThat(Files.isWritable(pathWithDifferentCase)).isTrue();
                    assertThat(Files.isExecutable(pathWithDifferentCase)).isTrue();
                    assertThat(Files.size(pathWithDifferentCase)).isEqualTo(0);
                    assertThat(Files.isSameFile(pathWithDifferentCase, file)).isTrue();
                }

                @Test
                void shouldFailWhenTryingToCreateFileInAnotherCase() {
                    Path pathWithDifferentCase = fs.getPath(testFilePath.toUpperCase());

                    assertThatThrownBy(() -> Files.createFile(pathWithDifferentCase))
                            .isInstanceOf(FileAlreadyExistsException.class);
                }

                @Nested
                class WriteShortContentToFile {
                    @BeforeEach
                    void setup() throws IOException {
                        Files.write(file, "Hello World!".getBytes());
                    }

                    @Test
                    void shouldWriteToFile() throws Exception {
                        assertThat(Files.size(file)).isEqualTo(12);
                        assertThat(Files.readAllBytes(file)).isEqualTo("Hello World!".getBytes());
                        assertThat(Files.isSameFile(file, file)).isTrue();
                    }

                    @Test
                    void shouldWriteLargeContentToFile() throws Exception {
                        Path largeFile = fs.getPath("C:\\largefile.txt");
                        Files.createFile(largeFile);

                        byte[] largeContent = new byte[1024 * 1024];
                        for (int i = 0; i < largeContent.length; i++) {
                            largeContent[i] = (byte) (i % 256);
                        }

                        Files.write(largeFile, largeContent);
                        assertThat(Files.readAllBytes(largeFile)).isEqualTo(largeContent);
                    }
                }

                @Test
                void shouldNotBeAbleToGetPosixFilePermissions() throws IOException {
                    assertThatThrownBy(() -> Files.getPosixFilePermissions(file)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Test
                void shouldNotBeAbleToSetPosixFilePermissions() throws IOException {
                    Set<PosixFilePermission> permissions = new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_READ));
                    assertThatThrownBy(() -> Files.setPosixFilePermissions(file, permissions)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Nested
                class WriteShortContentToFileWitAnotherCase {
                    Path fileWithAnotherCase;

                    @BeforeEach
                    void setup() throws IOException {
                        fileWithAnotherCase = fs.getPath(testFilePath.toUpperCase());
                        Files.write(fileWithAnotherCase, "Hello World!".getBytes());
                    }

                    @Test
                    void shouldWriteToFile() throws Exception {
                        assertThat(Files.size(fileWithAnotherCase)).isEqualTo(12);
                        assertThat(Files.readAllBytes(fileWithAnotherCase)).isEqualTo("Hello World!".getBytes());
                        assertThat(Files.isSameFile(fileWithAnotherCase, file)).isTrue();
                    }
                }

                @Nested
                class MakeFileReadOnly {
                    @BeforeEach
                    void setup() throws IOException {
                        Files.setAttribute(file, "dos:readonly", true);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        if (Files.exists(file)) {
                            Files.setAttribute(file, "dos:readonly", false);
                        }
                    }

                    @Test
                    void shouldMakeFileReadOnly() throws Exception {
                        assertThat(Files.isReadable(file)).isTrue();
                        assertThat(Files.isWritable(file)).isFalse();
                        assertThat(Files.isExecutable(file)).isTrue();
                        assertThat(Files.isSameFile(file, file)).isTrue();
                    }

                    @Test
                    void shouldNotBeAbleToWriteContent() throws IOException {
                        assertThatThrownBy(() -> Files.write(file, "Hello World!".getBytes())).isInstanceOf(AccessDeniedException.class);
                    }

                    @Test
                    void shouldNotBeAbleToDeleteTheFile() throws IOException {
                        assertThatThrownBy(() -> Files.delete(file)).isInstanceOf(IOException.class);
                        assertThat(Files.exists(file)).isTrue();
                    }

                    @Nested
                    class MakeWritable {
                        @BeforeEach
                        void setup() throws IOException {
                            Files.setAttribute(file, "dos:readonly", false);
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            if (Files.exists(file)) {
                                Files.setAttribute(file, "dos:readonly", true);
                            }
                        }

                        @Test
                        void shouldMakeWritable() throws Exception {
                            assertThat(Files.isReadable(file)).isTrue();
                            assertThat(Files.isWritable(file)).isTrue();
                            assertThat(Files.isExecutable(file)).isTrue();
                            assertThat(Files.isSameFile(file, file)).isTrue();
                        }

                        @Test
                        void shouldBeAbleToDeleteTheFile() throws Exception {
                            Files.delete(file);
                            assertThat(Files.exists(file)).isFalse();
                        }

                        @Test
                        void shouldBeAbleToWriteContent() throws Exception {
                            Files.write(file, "Hello World!".getBytes());
                            assertThat(Files.readAllBytes(file)).isEqualTo("Hello World!".getBytes());
                        }
                    }
                }
            }
        }

        @Nested
        class SimpleDirectoryTests {
            String testDirPath = "C:\\testdir";
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
                assertThatThrownBy(() -> Files.isHidden(dir)).isInstanceOf(IOException.class);
                assertThat(Files.isSymbolicLink(dir)).isFalse();
                assertThat(Files.isReadable(dir)).isFalse();
                assertThat(Files.isWritable(dir)).isFalse();
                assertThat(Files.isExecutable(dir)).isFalse();
                assertThat(dir.toString()).isEqualTo(testDirPath);
            }

            @Nested
            class CreateDirectory {

                @BeforeEach
                void setup() throws IOException {
                    Files.createDirectories(dir);
                }

                @AfterEach
                void teardown() throws IOException {
                    deleteRecursivelyIfExists(dir);
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
                class SubDirectory {
                    String subDirPath = "C:\\testdir\\sub";
                    Path subDir;

                    @BeforeEach
                    void setup() throws IOException {
                        subDir = fs.getPath(subDirPath);
                    }

                    @Test
                    void shouldNotExist() throws Exception {
                        assertThat(Files.exists(subDir)).isFalse();
                        assertThat(Files.isDirectory(subDir)).isFalse();
                        assertThat(Files.notExists(subDir)).isTrue();
                        assertThat(Files.isRegularFile(subDir)).isFalse();
                        assertThatThrownBy(() -> Files.isHidden(subDir)).isInstanceOf(IOException.class);
                        assertThat(Files.isSymbolicLink(subDir)).isFalse();
                        assertThat(Files.isReadable(subDir)).isFalse();
                        assertThat(Files.isWritable(subDir)).isFalse();
                        assertThat(Files.isExecutable(subDir)).isFalse();
                        assertThat(subDir.toString()).isEqualTo(subDirPath);
                    }

                    @Nested
                    class CreateSubDirectory {
                        @BeforeEach
                        void setup() throws IOException {
                            Files.createDirectories(subDir);
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            deleteRecursivelyIfExists(subDir);
                        }

                        @Test
                        void shouldCreateSubDirectory() throws Exception {
                            assertThat(Files.exists(subDir)).isTrue();
                            assertThat(Files.isDirectory(subDir)).isTrue();
                            assertThat(Files.notExists(subDir)).isFalse();
                        }
                    }
                }
            }
        }

        @Nested
        class NestedDirectoryTests {
            String testDirPath = "C:\\tmp\\testdir";
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
                assertThatThrownBy(() -> Files.isHidden(dir)).isInstanceOf(IOException.class);
                assertThat(Files.isSymbolicLink(dir)).isFalse();
                assertThat(Files.isReadable(dir)).isFalse();
                assertThat(Files.isWritable(dir)).isFalse();
                assertThat(Files.isExecutable(dir)).isFalse();
                assertThat(Files.isSameFile(dir, dir)).isTrue();
                assertThat(dir.toString()).isEqualTo(testDirPath);
            }

            @Test
            void shouldFailWhenTryingToEstimateSize() {
                assertThatThrownBy(() -> Files.size(dir)).isInstanceOf(NoSuchFileException.class);
            }

            @Nested
            class CreateDirectory {

                @BeforeEach
                void setup() throws IOException {
                    Files.createDirectories(dir);
                }

                @AfterEach
                void teardown() throws IOException {
                    deleteRecursivelyIfExists(dir);
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

                @Test
                void shouldFailWhenTryingToCreateSameDirectoryAgain() {
                    assertThatThrownBy(() -> Files.createDirectory(dir))
                            .isInstanceOf(FileAlreadyExistsException.class);
                }

                @Test
                void shouldFailWhenTryingToCreateFileWithSameNameAsDirectory() {
                    assertThatThrownBy(() -> Files.createFile(dir))
                            .isInstanceOf(AccessDeniedException.class);
                }

                @Test
                void shouldNotBeAbleToGetPosixFilePermissions() throws IOException {
                    assertThatThrownBy(() -> Files.getPosixFilePermissions(dir)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Test
                void shouldNotBeAbleToSetPosixFilePermissions() throws IOException {
                    Set<PosixFilePermission> permissions = new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_READ));
                    assertThatThrownBy(() -> Files.setPosixFilePermissions(dir, permissions)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Nested
                class MakeDirectoryReadonly {
                    @BeforeEach
                    void setup() throws IOException {
                        Files.setAttribute(dir, "dos:readonly", true);
                    }

                    @Test
                    void shouldNotHaveWritePermissions() throws IOException {
                        assertThat(Files.isReadable(dir)).isTrue();
                        assertThat(Files.isWritable(dir)).isTrue(); // Is writable, even if readonly is set - windows standard
                        assertThat(Files.isExecutable(dir)).isTrue();
                        assertThat(Files.getAttribute(dir, "dos:readonly")).isEqualTo(true);
                    }

                    @Nested
                    class MakeDirectory {
                        /*
                        The dos:readonly attribute on a directory does not prevent creating files or subdirectories
                         inside it.
                        Windows interprets this flag only for the directory entry itself, meaning it affects operations
                         such as renaming or deleting the directory.
                        */

                        Path subdir;

                        @BeforeEach
                        void setup() throws IOException {
                            subdir = dir.resolve("testdir");
                            Files.createDirectory(subdir);
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            deleteRecursivelyIfExists(subdir);
                        }

                        @Test
                        void shouldCreateDirectory() {
                            assertThat(Files.exists(subdir)).isTrue();
                        }
                    }

                    @Nested
                    class CreateFile {
                        Path file;

                        @BeforeEach
                        void setup() throws IOException {
                            file = dir.resolve("testfile.txt");
                            Files.createFile(file);
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            deleteRecursivelyIfExists(file);
                        }

                        @Test
                        void shouldCreateFile() {
                            assertThat(Files.exists(file)).isTrue();
                        }
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
                        assertThatThrownBy(() -> Files.isHidden(dir)).isInstanceOf(IOException.class);
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
                            assertThatThrownBy(() -> Files.isHidden(dir)).isInstanceOf(IOException.class);
                            assertThat(Files.isSymbolicLink(dir)).isFalse();
                            assertThat(Files.isReadable(dir)).isFalse();
                            assertThat(Files.isWritable(dir)).isFalse();
                            assertThat(Files.isExecutable(dir)).isFalse();
                        }
                    }
                }

                @Nested
                class CreateFileInDirectory {
                    Path fileInDir;

                    @BeforeEach
                    void setup() throws IOException {
                        fileInDir = dir.resolve("testfile.txt");
                        Files.createFile(fileInDir);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        deleteRecursivelyIfExists(fileInDir);
                    }

                    @Test
                    void shouldCreateFileInDirectory() throws Exception {
                        assertThat(Files.exists(fileInDir)).isTrue();
                        assertThat(Files.isRegularFile(fileInDir)).isTrue();
                        assertThat(Files.size(fileInDir)).isEqualTo(0);
                    }

                    @Test
                    void shouldShowFileInDir() throws IOException {
                        try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir)) {
                            Iterator<Path> iterator = entries.iterator();

                            Path nextPath = iterator.next();

                            assertThat(nextPath).isEqualTo(fileInDir);
                            assertThat(iterator.hasNext()).isFalse();
                        }
                    }

                    @Nested
                    class DeleteParentDirectoryRecursively {
                        @BeforeEach
                        void setup() throws IOException {
                            deleteRecursivelyIfExists(dir);
                        }

                        @Test
                        void shouldDeleteDirectory() throws IOException {
                            assertThat(Files.exists(dir)).isFalse();
                        }

                        @Test
                        void shouldAlsoDeleteFileInDirectory() {
                            assertThat(Files.exists(fileInDir)).isFalse();
                        }
                    }
                }
            }
        }

        @Nested
        class MoreNestedDirectoryTests {
            String testDirPath = "C:\\tmp\\a\\b\\c\\d\\testdir";
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
                assertThatThrownBy(() -> Files.isHidden(dir)).isInstanceOf(IOException.class);
                assertThat(Files.isSymbolicLink(dir)).isFalse();
                assertThat(Files.isReadable(dir)).isFalse();
                assertThat(Files.isWritable(dir)).isFalse();
                assertThat(Files.isExecutable(dir)).isFalse();
                assertThat(dir.toString()).isEqualTo(testDirPath);
            }

            @Nested
            class CreateDirectory {

                @BeforeEach
                void setup() throws IOException {
                    Files.createDirectories(dir);
                }

                @AfterEach
                void teardown() throws IOException {
                    deleteRecursivelyIfExists(dir);
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
                    String testDirPath = "C:\\tmp\\a\\b\\c";
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
                        assertThatThrownBy(() -> Files.isHidden(dir)).isInstanceOf(IOException.class);
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
                            assertThatThrownBy(() -> Files.isHidden(dir)).isInstanceOf(IOException.class);
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

    private static void deleteRecursivelyIfExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    makeWritable(entry);
                    deleteRecursivelyIfExists(entry);
                }
            }
        }

        makeWritable(path);
        Files.delete(path);
    }

    private static void makeWritable(Path path) throws IOException {
        Files.setAttribute(path, "dos:readonly", false);
    }
}
