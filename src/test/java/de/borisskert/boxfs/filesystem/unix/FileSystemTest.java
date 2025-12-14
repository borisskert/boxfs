package de.borisskert.boxfs.filesystem.unix;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
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
                    Iterator<Path> iterator = paths.iterator();
                    assertThat(iterator.hasNext()).isFalse();
                }
            }
        }

        @Nested
        class SimpleFileTests {
            String testFilePath = "/testfile.txt";
            Path root;
            Path file;

            @BeforeEach
            void setup() throws IOException {
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
            void shouldFindTwoFilesInRootDirectory() throws IOException {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                    Iterator<Path> iterator = paths.iterator();
                    assertThat(iterator.hasNext()).isFalse();
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
                    assertThat(Files.isExecutable(file)).isFalse();
                    assertThat(Files.size(file)).isEqualTo(0);
                    assertThat(Files.isSameFile(file, file)).isTrue();
                }

                @Test
                @Disabled
                void shouldFailWhenTryingToCreateSameFileAgain() {
                    assertThatThrownBy(() -> Files.createFile(file))
                            .isInstanceOf(FileAlreadyExistsException.class);
                }

                @Test
                void shouldFindTwoFilesInRootDirectory() throws IOException {
                    try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                        Iterator<Path> iterator = paths.iterator();

                        assertThat(iterator.hasNext()).isTrue();
                        assertThat(iterator.next()).isEqualTo(file);
                        assertThat(iterator.hasNext()).isFalse();
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
                    void shouldNotFindFileInAnotherCase() throws IOException {
                        assertThat(Files.exists(pathWithDifferentCase)).isFalse();
                        assertThat(Files.isDirectory(pathWithDifferentCase)).isFalse();
                        assertThat(Files.notExists(pathWithDifferentCase)).isTrue();
                        assertThat(Files.isRegularFile(pathWithDifferentCase)).isFalse();
                        assertThat(Files.isHidden(pathWithDifferentCase)).isFalse();
                        assertThat(Files.isSymbolicLink(pathWithDifferentCase)).isFalse();
                        assertThat(Files.isReadable(pathWithDifferentCase)).isFalse();
                        assertThat(Files.isWritable(pathWithDifferentCase)).isFalse();
                        assertThat(Files.isExecutable(pathWithDifferentCase)).isFalse();
                        assertThatThrownBy(() -> Files.size(pathWithDifferentCase)).isInstanceOf(IOException.class);
                        assertThatThrownBy(() -> Files.readAttributes(pathWithDifferentCase, "*")).isInstanceOf(IOException.class);
                        assertThatThrownBy(() -> Files.getLastModifiedTime(pathWithDifferentCase)).isInstanceOf(IOException.class);
                        assertThat(Files.isSameFile(pathWithDifferentCase, pathWithDifferentCase)).isTrue();
                        assertThat(pathWithDifferentCase.toString()).isEqualTo(testFilePathUpperCase);
                    }

                    @Test
                    void shouldFindTwoFilesInRootDirectory() throws IOException {
                        try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                            Iterator<Path> iterator = paths.iterator();

                            assertThat(iterator.hasNext()).isTrue();
                            assertThat(iterator.next()).isEqualTo(file);
                            assertThat(iterator.hasNext()).isFalse();
                        }
                    }

                    @Nested
                    class CreateFileInAnotherCase {
                        @BeforeEach
                        void setup() throws IOException {
                            Files.createFile(pathWithDifferentCase);
                        }

                        @AfterEach
                        void teardown() throws IOException {
                            Files.deleteIfExists(pathWithDifferentCase);
                        }

                        @Test
                        void shouldCreateFile() throws Exception {
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
                        void shouldLeaveTheOtherFile() throws IOException {
                            assertThat(Files.exists(file)).isTrue();
                            assertThat(Files.isSameFile(pathWithDifferentCase, file)).isFalse();
                        }

                        @Test
                        void shouldFindTwoFilesInRootDirectory() throws IOException {
                            try (DirectoryStream<Path> directorySteam = Files.newDirectoryStream(root)) {
                                Set<Path> paths = toSet(directorySteam.iterator());
                                assertThat(paths).containsExactly(file, pathWithDifferentCase);
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
                }

                @Test
                void shouldWriteToNotExistingFileWithAnotherCase() throws IOException {
                    Path fileWithAnotherCase = fs.getPath(testFilePath.toUpperCase());
                    Files.write(fileWithAnotherCase, "Hello World!".getBytes());

                    assertThat(Files.exists(fileWithAnotherCase)).isTrue();
                    assertThat(Files.size(fileWithAnotherCase)).isEqualTo(12);
                    assertThat(Files.readAllBytes(fileWithAnotherCase)).isEqualTo("Hello World!".getBytes());
                    assertThat(Files.isSameFile(fileWithAnotherCase, file)).isFalse();
                }

                @Nested
                class MakeFileReadOnly {
                    @BeforeEach
                    void setup() throws IOException {
                        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
                        permissions.remove(PosixFilePermission.OWNER_WRITE);
                        permissions.remove(PosixFilePermission.GROUP_WRITE);
                        permissions.remove(PosixFilePermission.OTHERS_WRITE);
                        Files.setPosixFilePermissions(file, permissions);
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
                assertThat(Files.isSameFile(dir, dir)).isTrue();
                assertThat(dir.toString()).isEqualTo(testDirPath);
            }

            @Test
            void shouldFailWhenTryingToEstimateSize() {
                assertThatThrownBy(() -> Files.size(dir)).isInstanceOf(IOException.class);
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
                class MakeDirectoryReadonly {
                    @BeforeEach
                    void setup() throws IOException {
                        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(dir);
                        permissions.remove(PosixFilePermission.OWNER_WRITE);
                        permissions.remove(PosixFilePermission.GROUP_WRITE);
                        permissions.remove(PosixFilePermission.OTHERS_WRITE);
                        Files.setPosixFilePermissions(dir, permissions);
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

                    @Test
                    void shouldFailingCreateFile() {
                        assertThatThrownBy(
                                () -> Files.createFile(dir.resolve("testfile.txt"))
                        ).isInstanceOf(IOException.class);
                    }

                    @Test
                    void shouldFailCreatingSubdirectory() {
                        assertThatThrownBy(
                                () -> Files.createDirectories(dir.resolve("testdir"))
                        ).isInstanceOf(IOException.class);
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

        Path parent = path.getParent();
        Set<PosixFilePermission> parentPermissions = Files.getPosixFilePermissions(parent);
        Set<PosixFilePermission> newParentPermissions = new HashSet<>(parentPermissions);
        newParentPermissions.add(PosixFilePermission.OWNER_WRITE);
        newParentPermissions.add(PosixFilePermission.GROUP_WRITE);
        newParentPermissions.add(PosixFilePermission.OTHERS_WRITE);
        Files.setPosixFilePermissions(parent, newParentPermissions);

        makeWritable(path);
        Files.delete(path);

        Files.setPosixFilePermissions(parent, parentPermissions);
    }

    private static void makeWritable(Path path) throws IOException {
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.OTHERS_WRITE);
        Files.setPosixFilePermissions(path, permissions);
    }

    private static <T> Set<T> toSet(Iterator<T> iterator) {
        Set<T> set = new HashSet<>();

        while (iterator.hasNext()) {
            set.add(iterator.next());
        }

        return set;
    }
}
