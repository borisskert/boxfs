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
            void setup() {
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

            @Test
            void shouldFindTwoFilesInRootDirectory() throws IOException {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                    Set<Path> files = toSet(paths.iterator());
                    assertThat(files).isEmpty();
                }
            }
        }

        @Nested
        class SimpleFileTests {
            String testFilePath = "C:\\testfile.txt";
            Path root;
            Path file;

            @BeforeEach
            void setup() {
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

            @Test
            void shouldNotFindAnyFilesInRootDirectory() throws IOException {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                    Iterator<Path> iterator = paths.iterator();
                    assertThat(iterator.hasNext()).isFalse();
                }
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

            @Test
            void shouldFailWhenTryingToCopyNonExistingFile() {
                assertThatThrownBy(() -> Files.copy(file, fs.getPath("target.txt")))
                        .isInstanceOf(NoSuchFileException.class);
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
                        Set<Path> files = toSet(paths.iterator());
                        assertThat(files).containsOnly(file);
                    }
                }

                @Nested
                class PathInUppercase {
                    String testFilePathUpperCase;
                    Path pathWithDifferentCase;

                    @BeforeEach
                    void setup() {
                        testFilePathUpperCase = testFilePath.toUpperCase();
                        pathWithDifferentCase = fs.getPath(testFilePathUpperCase);
                    }

                    @Test
                    void shouldFindFileInAnotherCase() throws IOException {
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
                        assertThat(Files.isSameFile(pathWithDifferentCase, pathWithDifferentCase)).isTrue();
                        assertThat(Files.isSameFile(pathWithDifferentCase, file)).isTrue();
                        assertThat(pathWithDifferentCase.toString()).isEqualTo(testFilePathUpperCase);
                    }

                    @Test
                    void shouldFindOneFileInRootDirectory() throws IOException {
                        try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                            Set<Path> files = toSet(paths.iterator());
                            assertThat(files).containsOnly(file);
                        }
                    }

                    @Nested
                    class CreateFileInAnotherCase {
                        // ... which isn't possible in Windows

                        @Test
                        void shouldFailCreatingFileWithDifferentCase() {
                            assertThatThrownBy(() -> Files.createFile(pathWithDifferentCase))
                                    .isInstanceOf(FileAlreadyExistsException.class);
                        }

                        @Test
                        void shouldFindFileInAnotherCase() throws Exception {
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
                            assertThat(Files.isSameFile(pathWithDifferentCase, pathWithDifferentCase)).isTrue();
                        }

                        @Test
                        void shouldLeaveTheExistingFile() throws IOException {
                            assertThat(Files.exists(file)).isTrue();
                            assertThat(Files.isSameFile(pathWithDifferentCase, file)).isTrue();
                        }

                        @Test
                        void shouldFindOneFileInRootDirectory() throws IOException {
                            try (DirectoryStream<Path> directorySteam = Files.newDirectoryStream(root)) {
                                Set<Path> paths = toSet(directorySteam.iterator());
                                assertThat(paths).containsOnly(file);
                            }
                        }

                        @Nested
                        class WriteShortContentToFileWitAnotherCase {
                            @BeforeEach
                            void setup() throws IOException {
                                Files.write(pathWithDifferentCase, "Hello World 2!".getBytes());
                            }

                            @Test
                            void shouldWriteToFile() throws Exception {
                                assertThat(Files.exists(pathWithDifferentCase)).isTrue();
                                assertThat(Files.size(pathWithDifferentCase)).isEqualTo(14);
                                assertThat(Files.readAllBytes(pathWithDifferentCase)).isEqualTo("Hello World 2!".getBytes());
                                assertThat(Files.isSameFile(pathWithDifferentCase, file)).isTrue();
                            }

                            @Test
                            void shouldHaveContentInOtherFile() throws Exception {
                                assertThat(Files.exists(file)).isTrue();
                                assertThat(Files.size(file)).isEqualTo(14);
                                assertThat(Files.readAllBytes(file)).isEqualTo("Hello World 2!".getBytes());
                                assertThat(Files.isSameFile(file, pathWithDifferentCase)).isTrue();
                            }
                        }
                    }
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

                    @Test
                    void shouldNotDoAnythingWhenCopyFileToSameTarget() throws IOException {
                        Files.copy(file, file);

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.notExists(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();
                        assertThat(Files.isHidden(file)).isFalse();
                        assertThat(Files.isSymbolicLink(file)).isFalse();
                        assertThat(Files.isReadable(file)).isTrue();
                        assertThat(Files.isWritable(file)).isTrue();
                        assertThat(Files.isExecutable(file)).isTrue();
                        assertThat(Files.size(file)).isEqualTo(12L);
                        assertThat(Files.readAllBytes(file)).isEqualTo("Hello World!".getBytes());
                        assertThat(Files.isSameFile(file, file)).isTrue();
                        assertThat(file.toString()).isEqualTo(testFilePath);
                    }
                }

                @Nested
                class CopyFileToAbsoluteSimpleTarget {
                    private Path target;

                    @BeforeEach
                    void setup() throws IOException {
                        target = fs.getPath("C:\\target.txt");
                        Files.write(file, "Hello World!".getBytes());
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        Files.deleteIfExists(target);
                    }

                    @Test
                    void shouldCopyFile() throws IOException {
                        Files.copy(file, target);

                        assertThat(Files.exists(target)).isTrue();
                        assertThat(Files.readAllBytes(target)).isEqualTo("Hello World!".getBytes());

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.readAllBytes(file)).isEqualTo("Hello World!".getBytes());
                    }
                }

                @Nested
                class CopyFileToRelativeSimpleTarget {
                    private Path target;

                    @BeforeEach
                    void setup() throws IOException {
                        target = fs.getPath("target.txt");
                        Files.write(file, "Hello World!".getBytes());
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        Files.deleteIfExists(target);
                    }

                    @Test
                    void shouldCopyFile() throws IOException {
                        Files.copy(file, target);

                        assertThat(Files.exists(target)).isTrue();
                        assertThat(Files.readAllBytes(target)).isEqualTo("Hello World!".getBytes());

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.readAllBytes(file)).isEqualTo("Hello World!".getBytes());
                    }
                }

                @Test
                void shouldNotDoAnythingWhenCopyEmptyFileToSameTarget() throws IOException {
                    Files.copy(file, file);

                    assertThat(Files.exists(file)).isTrue();
                    assertThat(Files.isDirectory(file)).isFalse();
                    assertThat(Files.notExists(file)).isFalse();
                    assertThat(Files.isRegularFile(file)).isTrue();
                    assertThat(Files.isHidden(file)).isFalse();
                    assertThat(Files.isSymbolicLink(file)).isFalse();
                    assertThat(Files.isReadable(file)).isTrue();
                    assertThat(Files.isWritable(file)).isTrue();
                    assertThat(Files.isExecutable(file)).isTrue();
                    assertThat(Files.size(file)).isEqualTo(0L);
                    assertThat(Files.isSameFile(file, file)).isTrue();
                    assertThat(file.toString()).isEqualTo(testFilePath);
                }

                @Test
                void shouldNotBeAbleToGetPosixFilePermissions() {
                    assertThatThrownBy(() -> Files.getPosixFilePermissions(file)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Test
                void shouldNotBeAbleToSetPosixFilePermissions() {
                    Set<PosixFilePermission> permissions = new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_READ));
                    assertThatThrownBy(() -> Files.setPosixFilePermissions(file, permissions)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Nested
                class MakeFileReadOnly {
                    Object oldValue;

                    @BeforeEach
                    void setup() throws IOException {
                        oldValue = makeReadonly(file);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        if (Files.exists(file)) {
                            Files.setAttribute(file, "dos:readonly", oldValue);
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
                    void shouldNotBeAbleToWriteContent() {
                        assertThatThrownBy(() -> Files.write(file, "Hello World!".getBytes())).isInstanceOf(IOException.class);
                    }
                }
            }
        }

        @Nested
        class SimpleDirectoryTests {
            String testDirPath = "C:\\testdir";
            Path dir;

            @BeforeEach
            void setup() {
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
                void shouldNotBeAbleToGetPosixFilePermissions() {
                    assertThatThrownBy(() -> Files.getPosixFilePermissions(dir)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Test
                void shouldNotBeAbleToSetPosixFilePermissions() {
                    Set<PosixFilePermission> permissions = new HashSet<>(Collections.singletonList(PosixFilePermission.OWNER_READ));
                    assertThatThrownBy(() -> Files.setPosixFilePermissions(dir, permissions)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Nested
                class MakeDirectoryReadonly {
                    Object oldValue;

                    @BeforeEach
                    void setup() throws IOException {
                        oldValue = makeReadonly(dir);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        if (Files.exists(dir)) {
                            Files.setAttribute(dir, "dos:readonly", oldValue);
                        }
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
                        /*
                        The dos:readonly attribute on a directory does not prevent creating files or subdirectories
                         inside it.
                        Windows interprets this flag only for the directory entry itself, meaning it affects operations
                         such as renaming or deleting the directory.
                        */
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
                    void shouldNotExist() {
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
                        void shouldNotExist() {
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
                            Set<Path> files = toSet(entries.iterator());
                            assertThat(files).containsExactly(fileInDir);
                        }
                    }

                    @Nested
                    class DeleteParentDirectoryRecursively {
                        @BeforeEach
                        void setup() throws IOException {
                            deleteRecursivelyIfExists(dir);
                        }

                        @Test
                        void shouldDeleteDirectory() {
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
            void setup() {
                dir = fs.getPath(testDirPath);
            }

            @Test
            void shouldNotExist() {
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
                    void shouldNotExist() {
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
                        void shouldNotExist() {
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

    private static Object makeReadonly(Path path) throws IOException {
        Object oldValue = Files.getAttribute(path, "dos:readonly");
        Files.setAttribute(path, "dos:readonly", true);

        return oldValue;
    }

    private static <T> Set<T> toSet(Iterator<T> iterator) {
        Set<T> set = new HashSet<>();

        while (iterator.hasNext()) {
            set.add(iterator.next());
        }

        return set;
    }
}
