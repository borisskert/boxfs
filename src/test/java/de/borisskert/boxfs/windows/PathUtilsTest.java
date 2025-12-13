package de.borisskert.boxfs.windows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.InvalidPathException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class PathUtilsTest {

    abstract String parentOf(String path);

    @Nested
    class Parent {
        @Test
        void shouldGetParentOfSuperSimplePath() {
            String parent = parentOf("C:\\directory");
            assertThat(parent).isEqualTo("C:\\");
        }

        @Test
        void shouldGetParentOfSimplePath() {
            String parent = parentOf("C:\\directory\\file.txt");
            assertThat(parent).isEqualTo("C:\\directory");
        }

        @Test
        void shouldGetParentOfSimpleNestedPath() {
            String parent = parentOf("C:\\a\\b\\c\\d");
            assertThat(parent).isEqualTo("C:\\a\\b\\c");
        }

        @Test
        void shouldReturnNullForRelativeDirectory() {
            assertThat(parentOf("directory")).isNull();
        }

        @Test
        void shouldGetParentOfRoot() {
            String parent = parentOf("C:\\");
            assertThat(parent).isNull();
        }
    }

    abstract boolean isAbsolute(String path);

    @Nested
    class IsAbsolute {
        @Test
        void shouldReturnTrueForDriveC() {
            assertThat(isAbsolute("C:\\")).isTrue();
        }

        @Test
        void shouldReturnFalseForDriveD() {
            assertThat(isAbsolute("D:")).isFalse();
        }

        @Test
        void shouldReturnFalseForRelativePaths() {
            assertThat(isAbsolute("directory")).isFalse();
        }

        @Test
        void shouldReturnFalseForEmptyPath() {
            assertThat(isAbsolute("")).isFalse();
        }

        @Test
        void shouldThrowForNull() {
            assertThatThrownBy(() -> isAbsolute(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldReturnFalseForRoot() {
            assertThat(isAbsolute("/")).isFalse();
        }

        @Test
        void shouldThrowForRootWithBackslash() {
            assertThatThrownBy(() -> isAbsolute("\\\\")).isInstanceOf(InvalidPathException.class);
        }

        @Test
        void shouldThrowForRootWithBackslashAndDriveLetter() {
            assertThatThrownBy(() -> isAbsolute("\\C:\\")).isInstanceOf(InvalidPathException.class);
        }

        @Test
        void shouldReturnFalseForSimpleFile() {
            assertThat(isAbsolute("file.txt")).isFalse();
        }

        @Test
        void shouldReturnFalseForSimpleFileStartingWithSlash() {
            assertThat(isAbsolute("\\file.txt")).isFalse();
        }

        @Test
        void shouldReturnFalseForSimpleFileStartingWithDot() {
            assertThat(isAbsolute(".\\file.txt")).isFalse();
        }
    }

    abstract String relativize(String path, String other);

    @Nested
    class Relativize {

        @Test
        void shouldRelativizeTheSameDirectory() {
            String relativized = relativize("C:\\directory", "C:\\directory");
            assertThat(relativized).isEqualTo("");
        }

        @Test
        void shouldRelativizeTheSameDirectoryWithEndingSlash() {
            String relativized = relativize("C:\\directory", "C:\\directory\\");
            assertThat(relativized).isEqualTo("");
        }

        @Test
        void shouldRelativizeTheSameDirectoryWithEndingSlashInOppositeDirection() {
            String relativized = relativize("C:\\directory\\", "C:\\directory");
            assertThat(relativized).isEqualTo("");
        }

        @Test
        void shouldRelativizeFileInSameDirectory() {
            String relativized = relativize("C:\\directory", "C:\\directory\\file.txt");
            assertThat(relativized).isEqualTo("file.txt");
        }

        @Test
        void shouldRelativizeFileInSameDirectoryWithEndingSeparator() {
            String relativized = relativize("C:\\directory\\", "C:\\directory\\file.txt");
            assertThat(relativized).isEqualTo("file.txt");
        }

        @Test
        void shouldRelativizeFileInSameDirectoryInOtherDirection() {
            String relativized = relativize("C:\\directory\\file.txt", "C:\\directory");
            assertThat(relativized).isEqualTo("..");
        }

        @Test
        void shouldRelativizeTwoLevels() {
            String relativized = relativize("C:\\a\\b\\c\\d", "C:\\a");
            assertThat(relativized).isEqualTo("..\\..\\..");
        }

        @Test
        void shouldRelativizeTwoLevelsInOppositeDirection() {
            String relativized = relativize("C:\\a", "C:\\a\\b\\c\\d");
            assertThat(relativized).isEqualTo("b\\c\\d");
        }

        @Test
        void shouldRelativizeNestedPath() {
            String relativized = relativize("C:\\tmp\\a\\b\\c\\d", "C:\\tmp\\a\\b\\c\\d\\test");
            assertThat(relativized).isEqualTo("test");
        }

        @Test
        void shouldRelativizeSimplePath() {
            String relativized = relativize("C:\\", "C:\\tmp\\testdir");
            assertThat(relativized).isEqualTo("tmp\\testdir");
        }
    }

    abstract String toAbsolutePath(String path);

    abstract String currentWorkingDirectory();

    @Nested
    class ToAbsolutePath {
        @Test
        void shouldReturnAbsolutePathOfSimplestRelativePath() {
            assertThat(toAbsolutePath("test")).isEqualTo(currentWorkingDirectory() + "\\test");
        }

        @Test
        void shouldReturnAbsolutePathOfSimpleRelativePath() {
            assertThat(toAbsolutePath("tmp\\test")).isEqualTo(currentWorkingDirectory() + "\\tmp\\test");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePath() {
            assertThat(toAbsolutePath("a\\b\\c\\d")).isEqualTo(currentWorkingDirectory() + "\\a\\b\\c\\d");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithEndingSlash() {
            assertThat(toAbsolutePath("a\\b\\c\\d\\")).isEqualTo(currentWorkingDirectory() + "\\a\\b\\c\\d");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithStartingSlash() {
            assertThat(toAbsolutePath("\\a\\b\\c\\d\\")).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithDoubleStartingSlash() {
            assertThat(toAbsolutePath("\\\\a\\b\\c\\d\\")).isEqualTo("\\\\a\\b\\c\\d");
        }

        @Test
        void shouldReturnAbsolutePathOfEmptyPath() {
            assertThat(toAbsolutePath("")).isEqualTo(currentWorkingDirectory());
        }

        @Test
        void shouldReturnNullOfNull() {
            assertThatThrownBy(() -> toAbsolutePath(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldReturnSamePathForAbsolutePath() {
            assertThat(toAbsolutePath("C:\\a\\b\\c\\d")).isEqualTo("C:\\a\\b\\c\\d");
        }
    }

    abstract String getFileName(String path);

    @Nested
    class GetFileName {
        @Test
        void shouldGetFileNameOfSimpleFile() {
            assertThat(getFileName("C:\\tmp\\test")).isEqualTo("test");
        }

        @Test
        void shouldGetFileNameOfSimpleFileWithExtension() {
            assertThat(getFileName("test.txt")).isEqualTo("test.txt");
        }

        @Test
        void shouldGetFileNameOfSimpleFileWithEndingSlash() {
            assertThat(getFileName("C:\\tmp\\test\\")).isEqualTo("test");
        }

        @Test
        void shouldReturnNullForNull() {
            assertThatThrownBy(() -> getFileName(null)).isInstanceOf(NullPointerException.class);
        }
    }

    abstract String getRoot(String path);

    @Nested
    class GetRoot {
        @Test
        void shouldGetRootOfRelativePath() {
            assertThat(getRoot("test")).isNull();
        }

        @Test
        void shouldGetRootOfEmptyPath() {
            assertThat(getRoot("")).isNull();
        }

        @Test
        void shouldGetRootOfNull() {
            assertThatThrownBy(() -> getRoot(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldGetRootForAbsolutePath() {
            assertThat(getRoot("C:\\a\\b\\c")).isEqualTo("C:\\");
        }

        @Test
        void shouldGetRootForAbsolutePathWithD() {
            assertThat(getRoot("D:\\a\\b\\c")).isEqualTo("D:\\");
        }

        @Test
        void shouldGetRootForDrivePathC() {
            assertThat(getRoot("C:\\")).isEqualTo("C:\\");
        }

        @Test
        void shouldGetRootForDrivePathD() {
            assertThat(getRoot("D:\\")).isEqualTo("D:\\");
        }

        @Test
        void shouldGetRootForDriveLetterD() {
            assertThat(getRoot("D:")).isEqualTo("D:");
        }

        @Test
        void shouldGetRootForDriveLetterC() {
            assertThat(getRoot("C:")).isEqualTo("C:");
        }
    }

    abstract int getNameCount(String path);

    @Nested
    class GetNameCount {
        @Test
        void shouldGetNameCountOfDirectoryAndFile() {
            assertThat(getNameCount("C:\\directory\\file.txt")).isEqualTo(2);
        }

        @Test
        void shouldGetNameCountOfNestedDirectories() {
            assertThat(getNameCount("C:\\a\\b\\c\\d")).isEqualTo(4);
        }

        @Test
        void shouldGetNameCountOfRootOnly() {
            assertThat(getNameCount("C:\\")).isEqualTo(0);
        }

        @Test
        void shouldGetNameCountOfRelativePath() {
            assertThat(getNameCount("directory\\file.txt")).isEqualTo(2);
        }

        @Test
        void shouldGetNameCountOfEmptyPath() {
            assertThat(getNameCount("")).isEqualTo(1);
        }

        @Test
        void shouldGetNameCountOfNull() {
            assertThatThrownBy(() -> getNameCount(null)).isInstanceOf(NullPointerException.class);
        }
    }

    abstract String getName(String path, int index);

    @Nested
    class GetName {
        @Test
        void shouldReturnNullForNull() {
            assertThatThrownBy(() -> getName(null, 0)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> getName(null, 1)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> getName(null, 2)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldReturnNameForName() {
            assertThat(getName("directory", 0)).isEqualTo("directory");
            assertThatThrownBy(() -> getName("directory", 1)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldGetNamesOfSimplePath() {
            assertThat(getName("C:\\directory\\file.txt", 0)).isEqualTo("directory");
            assertThat(getName("C:\\directory\\file.txt", 1)).isEqualTo("file.txt");
            assertThatThrownBy(() -> getName("C:\\directory\\file.txt", 2)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldGetNamesOfNestedDirectories() {
            assertThat(getName("C:\\a\\b\\c\\d\\", 0)).isEqualTo("a");
            assertThat(getName("C:\\a\\b\\c\\d\\", 1)).isEqualTo("b");
            assertThat(getName("C:\\a\\b\\c\\d\\", 2)).isEqualTo("c");
            assertThat(getName("C:\\a\\b\\c\\d\\", 3)).isEqualTo("d");
            assertThatThrownBy(() -> getName("C:\\a\\b\\c\\d\\", 4)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    abstract String subpath(String path, int beginIndex, int endIndex);

    @Nested
    class Subpath {

        @Nested
        class AbsolutePath {
            String path;

            @BeforeEach
            void setup() {
                path = "C:\\tmp\\test";
            }

            @Test
            void shouldReturnFor0To2() {
                assertThat(subpath(path, 0, 2)).isEqualTo("tmp\\test");
            }

            @Test
            void shouldReturnFor0To1() {
                assertThat(subpath(path, 0, 1)).isEqualTo("tmp");
            }

            @Test
            void shouldReturnFor1To2() {
                assertThat(subpath(path, 1, 2)).isEqualTo("test");
            }

            @Test
            void shouldReturnFor0To0() {
                assertThatThrownBy(() -> subpath(path, 0, 0)).isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            void shouldThrowFor1To3() {
                assertThatThrownBy(() -> subpath(path, 1, 3))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            void shouldThrowForMinus1To1() {
                assertThatThrownBy(() -> subpath(path, -1, 1))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Nested
        class RelativePath {
            String path;

            @BeforeEach
            void setup() {
                path = "tmp\\test";
            }

            @Test
            void shouldReturnFor0To2() {
                assertThat(subpath(path, 0, 2)).isEqualTo("tmp\\test");
            }

            @Test
            void shouldReturnFor0To1() {
                assertThat(subpath(path, 0, 1)).isEqualTo("tmp");
            }

            @Test
            void shouldReturnFor1To2() {
                assertThat(subpath(path, 1, 2)).isEqualTo("test");
            }

            @Test
            void shouldReturnFor0To0() {
                assertThatThrownBy(() -> subpath(path, 0, 0)).isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            void shouldThrowFor1To3() {
                assertThatThrownBy(() -> subpath(path, 1, 3))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            void shouldThrowForMinus1To1() {
                assertThatThrownBy(() -> subpath(path, -1, 1))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Test
        void shouldGetSubpathOfSimpleRelativePathStartingWithSeparator() {
            String path = "\\tmp\\test";

            assertThat(subpath(path, 0, 2)).isEqualTo("tmp\\test");
            assertThat(subpath(path, 0, 1)).isEqualTo("tmp");
            assertThat(subpath(path, 1, 2)).isEqualTo("test");
            assertThatThrownBy(() -> subpath(path, 0, 0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> subpath(path, 1, 3))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> subpath(path, -1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldGetSubpathOfSimpleRelativePathEndingWithSeparator() {
            String path = "tmp\\test\\";

            assertThat(subpath(path, 0, 2)).isEqualTo("tmp\\test");
            assertThat(subpath(path, 0, 1)).isEqualTo("tmp");
            assertThat(subpath(path, 1, 2)).isEqualTo("test");
            assertThatThrownBy(() -> subpath(path, 0, 0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> subpath(path, 1, 3))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> subpath(path, -1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldReturnNullForNull() {
            assertThatThrownBy(() -> subpath(null, 0, 0)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> subpath(null, 0, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldReturnForEmptyPath() {
            assertThatThrownBy(() -> subpath("", 0, 0)).isInstanceOf(IllegalArgumentException.class);
            assertThat(subpath("", 0, 1)).isEqualTo("");
        }
    }

    abstract String resolve(String path, String other);

    @Nested
    class Resolve {
        @Test
        void shouldResolveRelativePathAgainstAbsolutePath() {
            String result = resolve("C:\\a\\b", "c\\d");
            assertThat(result).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathAgainstAbsolutePath() {
            String result = resolve("C:\\a\\b", "D:\\c\\d");
            assertThat(result).isEqualTo("D:\\c\\d");
        }

        @Test
        void shouldResolveRelativePathAgainstRelativePath() {
            String result = resolve("a\\b", "c\\d");
            assertThat(result).isEqualTo("a\\b\\c\\d");
        }

        @Test
        void shouldResolveRelativePathWithEndingSeparatorAgainstRelativePath() {
            String result = resolve("a\\b\\", "c\\d");
            assertThat(result).isEqualTo("a\\b\\c\\d");
        }

        @Test
        void shouldResolveRelativePathAgainstRelativePathWithStartingSeparator() {
            String result = resolve("a\\b", "\\c\\d");
            assertThat(result).isEqualTo("\\c\\d");
        }

        @Test
        void shouldResolveRelativePathWithEndlingSeparatorAgainstRelativePathWithStartingSeparator() {
            String result = resolve("a\\b\\", "\\c\\d");
            assertThat(result).isEqualTo("\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathAgainstRelativePathWithStartingSeparator() {
            String result = resolve("C:\\a\\b", "\\c\\d");
            assertThat(result).isEqualTo("C:\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathWithEndingSeparatorAgainstRelativePathWithStartingSeparator() {
            String result = resolve("C:\\a\\b\\", "\\c\\d");
            assertThat(result).isEqualTo("C:\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathAgainstRelativePath() {
            String result = resolve("C:\\a\\b", "c\\d");
            assertThat(result).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathWithEndingSeparatorAgainstRelativePath() {
            String result = resolve("C:\\a\\b\\", "c\\d");
            assertThat(result).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldResolveTestDir() {
            String result = resolve("C:\\", "tmp\\testdir");
            assertThat(result).isEqualTo("C:\\tmp\\testdir");
        }
    }

    abstract java.util.Iterator<String> iterator(String path);

    @Nested
    class Iterator {
        @Test
        void shouldThrowWhenIterateOverNull() {
            assertThatThrownBy(() -> iterator(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldIterateOverEmpty() {
            java.util.Iterator<String> iterator = iterator("");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverRootC() {
            java.util.Iterator<String> iterator = iterator("C:\\");
            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverRootD() {
            java.util.Iterator<String> iterator = iterator("D:\\");
            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverDriveC() {
            java.util.Iterator<String> iterator = iterator("C:");
            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleAbsoluteNestedPath() {
            String path = "C:\\tmp\\testDir";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleAbsoluteNestedPathWithEndingSeparator() {
            String path = "C:\\tmp\\testDir\\";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldThrowWhenIterateOverSimpleAbsoluteNestedPathWithStartingSeparator() {
            String path = "\\C:\\tmp\\testDir";
            assertThatThrownBy(() -> iterator(path)).isInstanceOf(InvalidPathException.class);
        }

        @Test
        void shouldThrowForSimpleAbsoluteNestedPathWithStartingAndEndingSeparator() {
            String path = "\\C:\\tmp\\testDir\\";
            assertThatThrownBy(() -> iterator(path)).isInstanceOf(InvalidPathException.class);
        }

        @Test
        void shouldIterateOverSimpleRelativeNestedPath() {
            String path = "tmp\\testDir";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleRelativeNestedPathWithStartingSeparator() {
            String path = "\\tmp\\testDir";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleRelativeNestedPathWithEndingSeparator() {
            String path = "tmp\\testDir\\";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleRelativeNestedPathWithStartingAndEndingSeparator() {
            String path = "\\tmp\\testDir\\";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }
    }
}
