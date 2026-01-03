package de.borisskert.boxfs.filesystem.macos;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
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
                assertThat(root.toString()).isEqualTo("/");
                assertThat(root.toAbsolutePath().toString()).isEqualTo("/");
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
            String testFilePath = "/testfile.txt";
            Path root;
            Path file;

            @BeforeEach
            void setup() {
                root = fs.getPath("/");
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
                assertThat(file.toString()).isEqualTo(testFilePath);
            }

            @Test
            void shouldNotFindAnyFilesInRootDirectory() throws IOException {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                    Set<Path> files = toSet(paths.iterator());
                    assertThat(files).isEmpty();
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
                Path target = fs.getPath("target.txt");

                assertThatThrownBy(() -> Files.copy(file, target))
                        .isInstanceOf(NoSuchFileException.class);

                assertThat(Files.exists(file)).isFalse();
                assertThat(Files.exists(target)).isFalse();
            }

            @Test
            void shouldFailWhenTryingToMoveNonExistingFile() {
                Path target = fs.getPath("target.txt");

                assertThatThrownBy(() -> Files.move(file, target))
                        .isInstanceOf(NoSuchFileException.class);

                assertThat(Files.exists(file)).isFalse();
                assertThat(Files.exists(target)).isFalse();
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
                        assertThat(Files.isExecutable(pathWithDifferentCase)).isFalse();
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
                        // ... which isn't possible in MacOS

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
                            assertThat(Files.isExecutable(pathWithDifferentCase)).isFalse();
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
                        Path largeFile = fs.getPath("/largefile.txt");
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
                        assertThat(Files.isExecutable(file)).isFalse();
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
                        target = fs.getPath("/target.txt");
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
                    assertThat(Files.isExecutable(file)).isFalse();
                    assertThat(Files.size(file)).isEqualTo(0L);
                    assertThat(Files.isSameFile(file, file)).isTrue();
                    assertThat(file.toString()).isEqualTo(testFilePath);
                }

                @Nested
                class MoveFileToAbsoluteSimpleTarget {
                    private Path target;

                    @BeforeEach
                    void setup() throws IOException {
                        target = fs.getPath("/target.txt");
                        Files.write(file, "Hello World!".getBytes());
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        Files.deleteIfExists(target);
                    }

                    @Test
                    void shouldMoveFile() throws IOException {
                        Files.move(file, target);

                        assertThat(Files.exists(target)).isTrue();
                        assertThat(Files.readAllBytes(target)).isEqualTo("Hello World!".getBytes());

                        assertThat(Files.exists(file)).isFalse();
                    }
                }

                @Nested
                class MoveFileToRelativeSimpleTarget {
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
                    void shouldMoveFile() throws IOException {
                        Files.move(file, target);

                        assertThat(Files.exists(target)).isTrue();
                        assertThat(Files.readAllBytes(target)).isEqualTo("Hello World!".getBytes());

                        assertThat(Files.exists(file)).isFalse();
                    }
                }

                @Test
                void shouldNotDoAnythingWhenMoveEmptyFileToSameTarget() throws IOException {
                    Files.move(file, file);

                    assertThat(Files.exists(file)).isTrue();
                    assertThat(Files.isDirectory(file)).isFalse();
                    assertThat(Files.notExists(file)).isFalse();
                    assertThat(Files.isRegularFile(file)).isTrue();
                    assertThat(Files.isHidden(file)).isFalse();
                    assertThat(Files.isSymbolicLink(file)).isFalse();
                    assertThat(Files.isReadable(file)).isTrue();
                    assertThat(Files.isWritable(file)).isTrue();
                    assertThat(Files.isExecutable(file)).isFalse();
                    assertThat(Files.size(file)).isEqualTo(0L);
                    assertThat(Files.isSameFile(file, file)).isTrue();
                    assertThat(file.toString()).isEqualTo(testFilePath);
                }

                @Test
                @Disabled
                void shouldNotBeAbleToGetDosFilePermissions() {
                    assertThatThrownBy(() -> Files.getAttribute(file, "dos:readonly")).isInstanceOf(UnsupportedOperationException.class);
                }

                @Test
                @Disabled
                void shouldNotBeAbleToSetDosFilePermissions() {
                    assertThatThrownBy(() -> Files.setAttribute(file, "dos:readonly", true)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Nested
                class MakeFileReadOnly {
                    Set<PosixFilePermission> oldPermissions;

                    @BeforeEach
                    void setup() throws IOException {
                        oldPermissions = makeReadonly(file);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        if (Files.exists(file)) {
                            Files.setPosixFilePermissions(file, oldPermissions);
                        }
                    }

                    @Test
                    void shouldMakeFileReadOnly() throws Exception {
                        assertThat(Files.isReadable(file)).isTrue();
                        assertThat(Files.isWritable(file)).isFalse();
                        assertThat(Files.isExecutable(file)).isFalse();
                        assertThat(Files.isSameFile(file, file)).isTrue();
                    }

                    @Test
                    void shouldNotBeAbleToWriteContent() {
                        assertThatThrownBy(() -> Files.write(file, "Hello World!".getBytes())).isInstanceOf(IOException.class);
                    }
                }

                @Nested
                class CreateSecondFile {
                    String secondFilePath = "/secondfile.txt";
                    Path secondFile;

                    @BeforeEach
                    void setup() throws IOException {
                        secondFile = fs.getPath(secondFilePath);
                        Files.createFile(secondFile);
                        Files.write(secondFile, "Hello World! (2)".getBytes());
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        deleteRecursivelyIfExists(secondFile);
                    }

                    @Test
                    void shouldHaveWrittenSecondFile() throws IOException {
                        assertThat(Files.exists(secondFile)).isTrue();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.notExists(secondFile)).isFalse();
                        assertThat(Files.isRegularFile(secondFile)).isTrue();
                        assertThat(Files.isHidden(secondFile)).isFalse();
                        assertThat(Files.isSymbolicLink(secondFile)).isFalse();
                        assertThat(Files.isReadable(secondFile)).isTrue();
                        assertThat(Files.isWritable(secondFile)).isTrue();
                        assertThat(Files.isExecutable(secondFile)).isFalse();
                        assertThat(Files.size(secondFile)).isEqualTo(16L);
                        assertThat(Files.readAllBytes(secondFile)).isEqualTo("Hello World! (2)".getBytes());
                        assertThat(Files.isSameFile(secondFile, file)).isFalse();
                        assertThat(Files.isSameFile(secondFile, secondFile)).isTrue();
                        assertThat(secondFile.toString()).isEqualTo(secondFilePath);
                    }

                    @Test
                    void shouldLeaveFirstFileUntouched() throws IOException {
                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.notExists(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();
                        assertThat(Files.isHidden(file)).isFalse();
                        assertThat(Files.isSymbolicLink(file)).isFalse();
                        assertThat(Files.isReadable(file)).isTrue();
                        assertThat(Files.isWritable(file)).isTrue();
                        assertThat(Files.isExecutable(file)).isFalse();
                        assertThat(Files.size(file)).isEqualTo(0L);
                        assertThat(Files.readAllBytes(file)).isEqualTo(new byte[0]);
                        assertThat(Files.isSameFile(file, secondFile)).isFalse();
                        assertThat(Files.isSameFile(file, file)).isTrue();
                        assertThat(file.toString()).isEqualTo(testFilePath);
                    }

                    @Test
                    void shouldFailWhenTryingToCopySecondFileToOtherWithoutReplace() {
                        assertThatThrownBy(() -> Files.copy(secondFile, file))
                                .isInstanceOf(IOException.class);

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();

                        assertThat(Files.exists(secondFile)).isTrue();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.isRegularFile(secondFile)).isTrue();
                    }

                    @Test
                    void shouldCopySecondFileToOtherWithReplace() throws IOException {
                        Files.copy(secondFile, file, REPLACE_EXISTING);

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.notExists(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();
                        assertThat(Files.isHidden(file)).isFalse();
                        assertThat(Files.isSymbolicLink(file)).isFalse();
                        assertThat(Files.isReadable(file)).isTrue();
                        assertThat(Files.isWritable(file)).isTrue();
                        assertThat(Files.isExecutable(file)).isFalse();
                        assertThat(Files.size(file)).isEqualTo(16L);
                        assertThat(Files.readAllBytes(file)).isEqualTo("Hello World! (2)".getBytes());

                        assertThat(Files.exists(secondFile)).isTrue();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.notExists(secondFile)).isFalse();
                        assertThat(Files.isRegularFile(secondFile)).isTrue();
                        assertThat(Files.isHidden(secondFile)).isFalse();
                        assertThat(Files.isSymbolicLink(secondFile)).isFalse();
                        assertThat(Files.isReadable(secondFile)).isTrue();
                        assertThat(Files.isWritable(secondFile)).isTrue();
                        assertThat(Files.isExecutable(secondFile)).isFalse();
                        assertThat(Files.size(secondFile)).isEqualTo(16L);
                        assertThat(Files.readAllBytes(secondFile)).isEqualTo("Hello World! (2)".getBytes());

                        assertThat(Files.isSameFile(file, secondFile)).isFalse();
                        assertThat(Files.isSameFile(file, file)).isTrue();
                        assertThat(file.toString()).isEqualTo(testFilePath);
                    }

                    @Test
                    void shouldFailWhenTryingToCopyOtherFileToSecondWithoutReplace() {
                        assertThatThrownBy(() -> Files.copy(file, secondFile))
                                .isInstanceOf(IOException.class);

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();

                        assertThat(Files.exists(secondFile)).isTrue();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.isRegularFile(secondFile)).isTrue();
                    }

                    @Test
                    void shouldCopyOtherFileToSecondWithReplace() throws IOException {
                        Files.copy(file, secondFile, REPLACE_EXISTING);

                        assertThat(Files.exists(secondFile)).isTrue();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.notExists(secondFile)).isFalse();
                        assertThat(Files.isRegularFile(secondFile)).isTrue();
                        assertThat(Files.isHidden(secondFile)).isFalse();
                        assertThat(Files.isSymbolicLink(secondFile)).isFalse();
                        assertThat(Files.isReadable(secondFile)).isTrue();
                        assertThat(Files.isWritable(secondFile)).isTrue();
                        assertThat(Files.isExecutable(secondFile)).isFalse();
                        assertThat(Files.size(secondFile)).isEqualTo(0L);
                        assertThat(Files.readAllBytes(secondFile)).isEqualTo(new byte[0]);

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.notExists(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();
                        assertThat(Files.isHidden(file)).isFalse();
                        assertThat(Files.isSymbolicLink(file)).isFalse();
                        assertThat(Files.isReadable(file)).isTrue();
                        assertThat(Files.isWritable(file)).isTrue();
                        assertThat(Files.isExecutable(file)).isFalse();
                        assertThat(Files.size(file)).isEqualTo(0L);
                        assertThat(Files.readAllBytes(file)).isEqualTo(new byte[0]);

                        assertThat(Files.isSameFile(secondFile, file)).isFalse();
                        assertThat(Files.isSameFile(secondFile, secondFile)).isTrue();
                        assertThat(secondFile.toString()).isEqualTo(secondFilePath);
                    }

                    @Test
                    void shouldFailWhenTryingToMoveSecondFileToOtherWithoutReplace() {
                        assertThatThrownBy(() -> Files.move(secondFile, file))
                                .isInstanceOf(IOException.class);

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();

                        assertThat(Files.exists(secondFile)).isTrue();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.isRegularFile(secondFile)).isTrue();
                    }

                    @Test
                    void shouldMoveSecondFileToOtherWithReplace() throws IOException {
                        Files.move(secondFile, file, REPLACE_EXISTING);

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.notExists(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();
                        assertThat(Files.isHidden(file)).isFalse();
                        assertThat(Files.isSymbolicLink(file)).isFalse();
                        assertThat(Files.isReadable(file)).isTrue();
                        assertThat(Files.isWritable(file)).isTrue();
                        assertThat(Files.isExecutable(file)).isFalse();
                        assertThat(Files.size(file)).isEqualTo(16L);
                        assertThat(Files.readAllBytes(file)).isEqualTo("Hello World! (2)".getBytes());

                        assertThat(Files.exists(secondFile)).isFalse();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.notExists(secondFile)).isTrue();
                        assertThat(Files.isRegularFile(secondFile)).isFalse();
                        assertThat(Files.isHidden(secondFile)).isFalse();
                        assertThat(Files.isSymbolicLink(secondFile)).isFalse();
                        assertThat(Files.isReadable(secondFile)).isFalse();
                        assertThat(Files.isWritable(secondFile)).isFalse();
                        assertThat(Files.isExecutable(secondFile)).isFalse();
                        assertThatThrownBy(() -> Files.size(secondFile))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThatThrownBy(() -> Files.readAllBytes(secondFile)).
                                isInstanceOf(NoSuchFileException.class);

                        assertThatThrownBy(() -> Files.isSameFile(file, secondFile))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThatThrownBy(() -> Files.isSameFile(secondFile, file))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThat(Files.isSameFile(file, file)).isTrue();
                        assertThat(file.toString()).isEqualTo(testFilePath);
                    }

                    @Test
                    void shouldFailWhenTryingToMoveOtherFileToSecondWithoutReplace() {
                        assertThatThrownBy(() -> Files.move(file, secondFile))
                                .isInstanceOf(IOException.class);

                        assertThat(Files.exists(file)).isTrue();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.isRegularFile(file)).isTrue();

                        assertThat(Files.exists(secondFile)).isTrue();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.isRegularFile(secondFile)).isTrue();
                    }

                    @Test
                    void shouldMoveOtherFileToSecondWithReplace() throws IOException {
                        Files.move(file, secondFile, REPLACE_EXISTING);

                        assertThat(Files.exists(secondFile)).isTrue();
                        assertThat(Files.isDirectory(secondFile)).isFalse();
                        assertThat(Files.notExists(secondFile)).isFalse();
                        assertThat(Files.isRegularFile(secondFile)).isTrue();
                        assertThat(Files.isHidden(secondFile)).isFalse();
                        assertThat(Files.isSymbolicLink(secondFile)).isFalse();
                        assertThat(Files.isReadable(secondFile)).isTrue();
                        assertThat(Files.isWritable(secondFile)).isTrue();
                        assertThat(Files.isExecutable(secondFile)).isFalse();
                        assertThat(Files.size(secondFile)).isEqualTo(0L);
                        assertThat(Files.readAllBytes(secondFile)).isEqualTo(new byte[0]);

                        assertThat(Files.exists(file)).isFalse();
                        assertThat(Files.isDirectory(file)).isFalse();
                        assertThat(Files.notExists(file)).isTrue();
                        assertThat(Files.isRegularFile(file)).isFalse();
                        assertThat(Files.isHidden(file)).isFalse();
                        assertThat(Files.isSymbolicLink(file)).isFalse();
                        assertThat(Files.isReadable(file)).isFalse();
                        assertThat(Files.isWritable(file)).isFalse();
                        assertThat(Files.isExecutable(file)).isFalse();
                        assertThatThrownBy(() -> Files.size(file))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThatThrownBy(() -> Files.readAllBytes(file)).
                                isInstanceOf(NoSuchFileException.class);

                        assertThatThrownBy(() -> Files.isSameFile(secondFile, file))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThatThrownBy(() -> Files.isSameFile(file, secondFile))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThat(Files.isSameFile(secondFile, secondFile)).isTrue();
                        assertThat(secondFile.toString()).isEqualTo(secondFilePath);
                    }
                }
            }
        }

        @Nested
        class SimpleDirectoryTests {
            String testDirPath = "/tmp/testdir";
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
                assertThat(Files.isHidden(dir)).isFalse();
                assertThat(Files.isSymbolicLink(dir)).isFalse();
                assertThat(Files.isReadable(dir)).isFalse();
                assertThat(Files.isWritable(dir)).isFalse();
                assertThat(Files.isExecutable(dir)).isFalse();
                assertThat(Files.isSameFile(dir, dir)).isTrue();
                assertThat(dir.toString()).isEqualTo(testDirPath);
            }

            @Test
            void shouldFailWhenTryingToEstimateSize() {
                assertThatThrownBy(() -> Files.size(dir)).isInstanceOf(IOException.class);
            }

            @Test
            void shouldFailToCopyNotExistingDirectory() {
                Path target = fs.getPath("/targetdir");

                assertThatThrownBy(() -> Files.copy(dir, target))
                        .isInstanceOf(NoSuchFileException.class);

                assertThat(Files.exists(dir)).isFalse();
                assertThat(Files.exists(target)).isFalse();
            }

            @Test
            void shouldFailToMoveNotExistingDirectory() {
                Path target = fs.getPath("/targetdir");

                assertThatThrownBy(() -> Files.move(dir, target))
                        .isInstanceOf(NoSuchFileException.class);

                assertThat(Files.exists(dir)).isFalse();
                assertThat(Files.exists(target)).isFalse();
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
                            .isInstanceOf(FileAlreadyExistsException.class);
                }

                @Test
                @Disabled
                void shouldNotBeAbleToGetDosFilePermissions() {
                    assertThatThrownBy(() -> Files.getAttribute(dir, "dos:readonly")).isInstanceOf(UnsupportedOperationException.class);
                }

                @Test
                @Disabled
                void shouldNotBeAbleToSetDosFilePermissions() {
                    assertThatThrownBy(() -> Files.setAttribute(dir, "dos:readonly", true)).isInstanceOf(UnsupportedOperationException.class);
                }

                @Nested
                class MakeDirectoryReadonly {
                    Set<PosixFilePermission> oldPermissions;

                    @BeforeEach
                    void setup() throws IOException {
                        oldPermissions = makeReadonly(dir);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        if (Files.exists(dir)) {
                            Files.setPosixFilePermissions(dir, oldPermissions);
                        }
                    }

                    @Test
                    void shouldNotHaveWritePermissions() throws IOException {
                        assertThat(Files.isReadable(dir)).isTrue();
                        assertThat(Files.isWritable(dir)).isFalse();
                        assertThat(Files.isExecutable(dir)).isTrue();
                        assertThat(Files.getPosixFilePermissions(dir)).containsAll(
                                Arrays.asList(
                                        PosixFilePermission.OWNER_READ,
                                        PosixFilePermission.OWNER_EXECUTE,
                                        PosixFilePermission.GROUP_READ,
                                        PosixFilePermission.GROUP_EXECUTE,
                                        PosixFilePermission.OTHERS_READ,
                                        PosixFilePermission.OTHERS_EXECUTE
                                )
                        );
                    }

                    @Nested
                    class MakeDirectory {
                        Path subdir;

                        @BeforeEach
                        void setup() {
                            subdir = dir.resolve("testdir");
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            deleteRecursivelyIfExists(subdir);
                        }

                        @Test
                        void shouldFailingCreateSubdirectory() {
                            assertThatThrownBy(() -> Files.createDirectory(subdir)).isInstanceOf(IOException.class);
                            assertThat(Files.exists(subdir)).isFalse();
                        }
                    }

                    @Nested
                    class CreateFile {
                        Path file;

                        @BeforeEach
                        void setup() {
                            file = dir.resolve("testfile.txt");
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            deleteRecursivelyIfExists(file);
                        }

                        @Test
                        void shouldFailingCreateFile() {
                            assertThatThrownBy(() -> Files.createFile(file)).isInstanceOf(IOException.class);
                            assertThat(Files.exists(file)).isFalse();
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

                    @Nested
                    class CopyDirectory {
                        String targetDirPath = "/targetdir";
                        Path target;

                        @BeforeEach
                        void setup() throws IOException {
                            target = fs.getPath(targetDirPath);
                            Files.copy(dir, target);
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            deleteRecursivelyIfExists(target);
                        }

                        @Test
                        void shouldCreateTargetDirectory() throws IOException {
                            assertThat(Files.exists(target)).isTrue();
                            assertThat(Files.isDirectory(target)).isTrue();
                            assertThat(Files.notExists(target)).isFalse();
                            assertThat(Files.isRegularFile(target)).isFalse();
                            assertThat(Files.isHidden(target)).isFalse();
                            assertThat(Files.isSymbolicLink(target)).isFalse();
                            assertThat(Files.isReadable(target)).isTrue();
                            assertThat(Files.isWritable(target)).isTrue();
                            assertThat(Files.isExecutable(target)).isTrue();
                        }

                        @Test
                        void shouldLeaveSourceDirectory() throws IOException {
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
                        void shouldNotCopyContainedFile() {
                            Path fileInTarget = target.resolve("testfile.txt");

                            assertThat(Files.exists(fileInTarget)).isFalse();
                            assertThat(Files.isRegularFile(fileInTarget)).isFalse();
                            assertThatThrownBy(() -> Files.size(fileInTarget)).isInstanceOf(NoSuchFileException.class);
                        }

                        @Test
                        void shouldLeaveExistingFileInSourceDir() throws IOException {
                            assertThat(Files.exists(fileInDir)).isTrue();
                            assertThat(Files.isRegularFile(fileInDir)).isTrue();
                            assertThat(Files.size(fileInDir)).isEqualTo(0);
                        }
                    }

                    @Nested
                    class MoveDirectory {
                        String targetDirPath = "/targetdir";
                        Path target;
                        Path targetFile;

                        @BeforeEach
                        void setup() throws IOException {
                            Files.write(fileInDir, "Hello World!".getBytes());

                            target = fs.getPath(targetDirPath);
                            targetFile = target.resolve("testfile.txt");

                            Files.move(dir, target);
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            Files.move(target, dir);
                        }

                        @Test
                        void shouldMoveDirectoryAndContentsToTarget() throws IOException {
                            assertThat(Files.exists(target)).isTrue();
                            assertThat(Files.isDirectory(target)).isTrue();
                            assertThat(Files.isRegularFile(target)).isFalse();

                            assertThat(Files.exists(targetFile)).isTrue();
                            assertThat(Files.isDirectory(targetFile)).isFalse();
                            assertThat(Files.isRegularFile(targetFile)).isTrue();

                            assertThat(Files.size(targetFile)).isEqualTo(12L);
                            assertThat(Files.readAllBytes(targetFile)).isEqualTo("Hello World!".getBytes());
                        }

                        @Test
                        void shouldRemoveDirectoryAndContentsFromSource() {
                            assertThat(Files.exists(dir)).isFalse();
                            assertThat(Files.isDirectory(dir)).isFalse();
                            assertThat(Files.isRegularFile(dir)).isFalse();

                            assertThat(Files.exists(fileInDir)).isFalse();
                            assertThat(Files.isDirectory(fileInDir)).isFalse();
                            assertThat(Files.isRegularFile(fileInDir)).isFalse();
                        }
                    }
                }

                @Nested
                class CopyDirectoryToAbsoluteSimpleTarget {
                    private Path target;

                    @BeforeEach
                    void setup() throws IOException {
                        target = fs.getPath("/copyoftestdir");
                        Files.copy(dir, target);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        deleteRecursivelyIfExists(target);
                    }

                    @Test
                    void shouldCopyDirectory() throws IOException {
                        assertThat(Files.exists(target)).isTrue();
                        assertThat(Files.isDirectory(target)).isTrue();
                        assertThat(Files.notExists(target)).isFalse();
                        assertThat(Files.isRegularFile(target)).isFalse();
                        assertThat(Files.isHidden(target)).isFalse();
                        assertThat(Files.isSymbolicLink(target)).isFalse();
                        assertThat(Files.isReadable(target)).isTrue();
                        assertThat(Files.isWritable(target)).isTrue();
                        assertThat(Files.isExecutable(target)).isTrue();
                    }

                    @Test
                    void shouldLeaveSourceDirectory() throws IOException {
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
                }

                @Nested
                class CopyDirectoryToRelativeSimpleTarget {
                    private Path target;

                    @BeforeEach
                    void setup() throws IOException {
                        target = fs.getPath("copyoftestdir");
                        Files.copy(dir, target);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        deleteRecursivelyIfExists(target);
                    }

                    @Test
                    void shouldCopyDirectory() throws IOException {
                        assertThat(Files.exists(target)).isTrue();
                        assertThat(Files.isDirectory(target)).isTrue();
                        assertThat(Files.notExists(dir)).isFalse();
                        assertThat(Files.isRegularFile(dir)).isFalse();
                        assertThat(Files.isHidden(dir)).isFalse();
                        assertThat(Files.isSymbolicLink(dir)).isFalse();
                        assertThat(Files.isReadable(dir)).isTrue();
                        assertThat(Files.isWritable(dir)).isTrue();
                        assertThat(Files.isExecutable(dir)).isTrue();
                    }
                }

                @Test
                void shouldNotDoAnythingWhenCopyFileToSameTarget() throws IOException {
                    Files.copy(dir, dir);

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
                void shouldNotDoAnythingWhenCopyFileToSameTargetWIthReplaceExisting() throws IOException {
                    Files.copy(dir, dir, REPLACE_EXISTING);

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
                class MoveDirectoryToAbsoluteSimpleTarget {
                    private Path target;

                    @BeforeEach
                    void setup() throws IOException {
                        target = fs.getPath("/targetdir");
                        Files.move(dir, target);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        Files.move(target, dir);
                    }

                    @Test
                    void shouldCreateSourceInTarget() throws IOException {
                        assertThat(Files.exists(target)).isTrue();
                        assertThat(Files.isDirectory(target)).isTrue();
                        assertThat(Files.notExists(target)).isFalse();
                        assertThat(Files.isRegularFile(target)).isFalse();
                        assertThat(Files.isHidden(target)).isFalse();
                        assertThat(Files.isSymbolicLink(target)).isFalse();
                        assertThat(Files.isReadable(target)).isTrue();
                        assertThat(Files.isWritable(target)).isTrue();
                        assertThat(Files.isExecutable(target)).isTrue();
                    }

                    @Test
                    void shouldDeleteSource() throws IOException {
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

                @Nested
                class MoveDirectoryToRelativeSimpleTarget {
                    private Path target;

                    @BeforeEach
                    void setup() throws IOException {
                        target = fs.getPath("targetdir");
                        Files.move(dir, target);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        Files.move(target, dir);
                    }

                    @Test
                    void shouldCreateSourceInTarget() throws IOException {
                        assertThat(Files.exists(target)).isTrue();
                        assertThat(Files.isDirectory(target)).isTrue();
                        assertThat(Files.notExists(target)).isFalse();
                        assertThat(Files.isRegularFile(target)).isFalse();
                        assertThat(Files.isHidden(target)).isFalse();
                        assertThat(Files.isSymbolicLink(target)).isFalse();
                        assertThat(Files.isReadable(target)).isTrue();
                        assertThat(Files.isWritable(target)).isTrue();
                        assertThat(Files.isExecutable(target)).isTrue();
                    }

                    @Test
                    void shouldDeleteSource() throws IOException {
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

                @Test
                void shouldNotDoAnythingWhenMoveFileToSameTarget() throws IOException {
                    Files.move(dir, dir);

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
                void shouldNotDoAnythingWhenMoveFileToSameTargetWIthReplaceExisting() throws IOException {
                    Files.move(dir, dir, REPLACE_EXISTING);

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
                class CreateSecondDirectory {
                    String secondDirPath = "/seconddir";
                    Path secondDir;

                    @BeforeEach
                    void setup() throws IOException {
                        secondDir = fs.getPath(secondDirPath);
                        Files.createDirectory(secondDir);
                    }

                    @AfterEach
                    void teardown() throws IOException {
                        deleteRecursivelyIfExists(secondDir);
                    }

                    @Test
                    void shouldCreateSecondDirectory() {
                        assertThat(Files.exists(secondDir)).isTrue();
                        assertThat(Files.isDirectory(secondDir)).isTrue();
                        assertThat(Files.isReadable(secondDir)).isTrue();
                        assertThat(Files.isWritable(secondDir)).isTrue();
                        assertThat(Files.isExecutable(secondDir)).isTrue();
                    }

                    @Test
                    void shouldFailWhenTryingToCopySecondDirectoryToOtherWithoutReplace() {
                        assertThatThrownBy(() -> Files.copy(secondDir, dir))
                                .isInstanceOf(IOException.class);
                    }

                    @Test
                    void shouldFailWhenTryingToCopyOtherDirectoryToSecondWithoutReplace() {
                        assertThatThrownBy(() -> Files.copy(dir, secondDir))
                                .isInstanceOf(FileAlreadyExistsException.class);
                    }

                    @Test
                    void shouldCopySecondDirectoryToOtherWithReplace() throws IOException {
                        Files.copy(secondDir, dir, REPLACE_EXISTING);

                        assertThat(Files.exists(dir)).isTrue();
                        assertThat(Files.isDirectory(dir)).isTrue();
                        assertThat(Files.isReadable(dir)).isTrue();
                        assertThat(Files.isWritable(dir)).isTrue();
                        assertThat(Files.isExecutable(dir)).isTrue();

                        assertThat(Files.exists(secondDir)).isTrue();
                        assertThat(Files.isDirectory(secondDir)).isTrue();
                        assertThat(Files.isReadable(secondDir)).isTrue();
                        assertThat(Files.isWritable(secondDir)).isTrue();
                        assertThat(Files.isExecutable(secondDir)).isTrue();

                        assertThat(Files.isSameFile(dir, secondDir)).isFalse();
                        assertThat(Files.isSameFile(secondDir, dir)).isFalse();
                        assertThat(Files.isSameFile(dir, dir)).isTrue();
                        assertThat(Files.isSameFile(secondDir, secondDir)).isTrue();

                        assertThat(dir.toString()).isEqualTo(testDirPath);
                        assertThat(secondDir.toString()).isEqualTo(secondDirPath);
                    }

                    @Test
                    void shouldFailWhenTryingToMoveOtherDirectoryToSecondWithoutReplace() {
                        assertThatThrownBy(() -> Files.move(dir, secondDir))
                                .isInstanceOf(FileAlreadyExistsException.class);

                        assertThat(Files.exists(dir)).isTrue();
                        assertThat(Files.isDirectory(dir)).isTrue();
                    }

                    @Test
                    void shouldMoveDirectoryToSecondWithReplace() throws IOException {
                        Files.move(dir, secondDir, REPLACE_EXISTING);

                        assertThat(Files.exists(dir)).isFalse();
                        assertThat(Files.isDirectory(dir)).isFalse();
                        assertThat(Files.isReadable(dir)).isFalse();
                        assertThat(Files.isWritable(dir)).isFalse();
                        assertThat(Files.isExecutable(dir)).isFalse();

                        assertThat(Files.exists(secondDir)).isTrue();
                        assertThat(Files.isDirectory(secondDir)).isTrue();
                        assertThat(Files.isReadable(secondDir)).isTrue();
                        assertThat(Files.isWritable(secondDir)).isTrue();
                        assertThat(Files.isExecutable(secondDir)).isTrue();

                        assertThatThrownBy(() -> Files.isSameFile(dir, secondDir))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThatThrownBy(() -> Files.isSameFile(secondDir, dir))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThat(Files.isSameFile(dir, dir)).isTrue();
                        assertThat(Files.isSameFile(secondDir, secondDir)).isTrue();

                        assertThat(dir.toString()).isEqualTo(testDirPath);
                        assertThat(secondDir.toString()).isEqualTo(secondDirPath);
                    }

                    @Test
                    void shouldMoveSecondDirectoryToOtherWithReplace() throws IOException {
                        Files.move(secondDir, dir, REPLACE_EXISTING);

                        assertThat(Files.exists(secondDir)).isFalse();
                        assertThat(Files.isDirectory(secondDir)).isFalse();
                        assertThat(Files.isReadable(secondDir)).isFalse();
                        assertThat(Files.isWritable(secondDir)).isFalse();
                        assertThat(Files.isExecutable(secondDir)).isFalse();

                        assertThat(Files.exists(dir)).isTrue();
                        assertThat(Files.isDirectory(dir)).isTrue();
                        assertThat(Files.isReadable(dir)).isTrue();
                        assertThat(Files.isWritable(dir)).isTrue();
                        assertThat(Files.isExecutable(dir)).isTrue();

                        assertThatThrownBy(() -> Files.isSameFile(dir, secondDir))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThatThrownBy(() -> Files.isSameFile(secondDir, dir))
                                .isInstanceOf(NoSuchFileException.class);
                        assertThat(Files.isSameFile(dir, dir)).isTrue();
                        assertThat(Files.isSameFile(secondDir, secondDir)).isTrue();

                        assertThat(dir.toString()).isEqualTo(testDirPath);
                        assertThat(secondDir.toString()).isEqualTo(secondDirPath);
                    }
                }
            }
        }

        @Nested
        class MoreNestedDirectoryTests {
            String testDirPath = "/tmp/a/b/c/d/testdir";
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
                assertThat(Files.isHidden(dir)).isFalse();
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

    private static void deleteRecursivelyIfExists(Path path) throws IOException {
        Path absolutePath = path.toAbsolutePath();

        if (!Files.exists(absolutePath)) {
            return;
        }

        if (Files.isDirectory(absolutePath)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(absolutePath)) {
                for (Path entry : entries) {
                    makeWritable(entry);
                    deleteRecursivelyIfExists(entry);
                }
            }
        }

        Path parent = absolutePath.getParent();
        Set<PosixFilePermission> parentPermissions = makeWritable(parent);

        makeWritable(absolutePath);
        Files.delete(absolutePath);

        Files.setPosixFilePermissions(parent, parentPermissions);
    }

    private static Set<PosixFilePermission> makeWritable(Path path) throws IOException {
        Set<PosixFilePermission> oldPermissions = Files.getPosixFilePermissions(path);
        Set<PosixFilePermission> newPermissions = new HashSet<>(oldPermissions);

        newPermissions.add(PosixFilePermission.OWNER_WRITE);
        newPermissions.add(PosixFilePermission.GROUP_WRITE);
        newPermissions.add(PosixFilePermission.OTHERS_WRITE);
        Files.setPosixFilePermissions(path, newPermissions);

        return oldPermissions;
    }

    private static Set<PosixFilePermission> makeReadonly(Path path) throws IOException {
        Set<PosixFilePermission> oldPermissions = Files.getPosixFilePermissions(path);
        Set<PosixFilePermission> newPermissions = new HashSet<>(oldPermissions);

        newPermissions.remove(PosixFilePermission.OWNER_WRITE);
        newPermissions.remove(PosixFilePermission.GROUP_WRITE);
        newPermissions.remove(PosixFilePermission.OTHERS_WRITE);
        Files.setPosixFilePermissions(path, newPermissions);

        return oldPermissions;
    }

    private static <T> Set<T> toSet(Iterator<T> iterator) {
        Set<T> set = new HashSet<>();

        while (iterator.hasNext()) {
            set.add(iterator.next());
        }

        return set;
    }
}
