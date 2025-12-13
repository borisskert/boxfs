package de.borisskert.boxfs.windows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoxFsPathsTest {

    @Nested
    class Parent {
        @Test
        void shouldGetParentOfSuperSimplePath() {
            String parent = BoxFsPaths.parentOf("C:\\directory");
            assertThat(parent).isEqualTo("C:\\");
        }

        @Test
        void shouldGetParentOfSimplePath() {
            String parent = BoxFsPaths.parentOf("C:\\directory\\file.txt");
            assertThat(parent).isEqualTo("C:\\directory");
        }

        @Test
        void shouldGetParentOfSimpleNestedPath() {
            String parent = BoxFsPaths.parentOf("C:\\a\\b\\c\\d");
            assertThat(parent).isEqualTo("C:\\a\\b\\c");
        }

        @Test
        void shouldReturnNullForRelativeDirectory() {
            assertThat(BoxFsPaths.parentOf("directory")).isNull();
        }
    }

    @Nested
    class IsAbsolute {
        @Test
        void shouldReturnTrueForDriveC() {
            assertThat(BoxFsPaths.isAbsolute("C:\\")).isTrue();
        }

        @Test
        void shouldReturnTrueForDriveD() {
            assertThat(BoxFsPaths.isAbsolute("D:")).isTrue();
        }

        @Test
        void shouldReturnFalseForRelativePaths() {
            assertThat(BoxFsPaths.isAbsolute("directory")).isFalse();
        }

        @Test
        void shouldReturnFalseForEmptyPath() {
            assertThat(BoxFsPaths.isAbsolute("")).isFalse();
        }

        @Test
        void shouldReturnFalseForNull() {
            assertThat(BoxFsPaths.isAbsolute(null)).isFalse();
        }

        @Test
        void shouldReturnFalseForRoot() {
            assertThat(BoxFsPaths.isAbsolute("/")).isFalse();
        }

        @Test
        void shouldReturnFalseForRootWithBackslash() {
            assertThat(BoxFsPaths.isAbsolute("\\\\")).isFalse();
        }

        @Test
        void shouldReturnFalseForRootWithBackslashAndDriveLetter() {
            assertThat(BoxFsPaths.isAbsolute("\\C:\\")).isFalse();
        }

        @Test
        void shouldReturnFalseForSimpleFile() {
            assertThat(BoxFsPaths.isAbsolute("file.txt")).isFalse();
        }

        @Test
        void shouldReturnFalseForSimpleFileStartingWithSlash() {
            assertThat(BoxFsPaths.isAbsolute("\\file.txt")).isFalse();
        }

        @Test
        void shouldReturnFalseForSimpleFileStartingWithDot() {
            assertThat(BoxFsPaths.isAbsolute(".\\file.txt")).isFalse();
        }
    }

    @Nested
    class Relativize {

        @Test
        void shouldRelativizeFileInSameDirectory() {
            String parent = BoxFsPaths.relativize("C:\\directory", "C:\\directory\\file.txt");
            assertThat(parent).isEqualTo("file.txt");
        }

        @Test
        void shouldRelativizeFileInSameDirectoryInOtherDirection() { // TODO dont know result
            String parent = BoxFsPaths.relativize("C:\\directory\\file.txt", "C:\\directory");
            assertThat(parent).isEqualTo("file.txt");
        }

        @Test
        void shouldRelativizeTwoLevels() {
            String parent = BoxFsPaths.relativize("C:\\a\\b\\c\\d", "C:\\a");
            assertThat(parent).isEqualTo("..\\..\\..");
        }

        @Test
        void shouldRelativizeTwoLevelsInOppositeDirection() {
            String parent = BoxFsPaths.relativize("C:\\a", "C:\\a\\b\\c\\d");
            assertThat(parent).isEqualTo("b\\c\\d");
        }

        @Test
        void shouldRelativizeSimplePath() {
            String parent = BoxFsPaths.relativize("C:\\tmp\\a\\b\\c\\d", "C:\\tmp\\a\\b\\c\\d\\test");
            assertThat(parent).isEqualTo("test");
        }
    }

    @Nested
    class ToAbsolutePath {
        @Test
        void shouldReturnAbsolutePathOfSimplestRelativePath() {
            assertThat(BoxFsPaths.toAbsolutePath("test")).isEqualTo("C:\\test");
        }

        @Test
        void shouldReturnAbsolutePathOfSimpleRelativePath() {
            assertThat(BoxFsPaths.toAbsolutePath("tmp\\test")).isEqualTo("C:\\tmp\\test");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePath() {
            assertThat(BoxFsPaths.toAbsolutePath("a\\b\\c\\d")).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithEndingSlash() {
            assertThat(BoxFsPaths.toAbsolutePath("a\\b\\c\\d\\")).isEqualTo("C:\\a\\b\\c\\d\\");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithStartingSlash() {
            assertThat(BoxFsPaths.toAbsolutePath("\\a\\b\\c\\d\\")).isEqualTo("C:\\a\\b\\c\\d\\");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithDoubleStartingSlash() {
            assertThat(BoxFsPaths.toAbsolutePath("\\\\a\\b\\c\\d\\")).isEqualTo("C:\\a\\b\\c\\d\\");
        }

        @Test
        void shouldReturnAbsolutePathOfEmptyPath() {
            assertThat(BoxFsPaths.toAbsolutePath("")).isEqualTo("C:\\");
        }

        @Test
        void shouldReturnNullOfNull() {
            assertThat(BoxFsPaths.toAbsolutePath(null)).isNull();
        }

        @Test
        void shouldReturnSamePathForAbsolutePath() {
            assertThat(BoxFsPaths.toAbsolutePath("C:\\a\\b\\c\\d")).isEqualTo("C:\\a\\b\\c\\d");
        }
    }

    @Nested
    class GetFileName {
        @Test
        void shouldGetFileNameOfSimpleFile() {
            assertThat(BoxFsPaths.getFileName("C:\\tmp\\test")).isEqualTo("test");
        }

        @Test
        void shouldGetFileNameOfSimpleFileWithExtension() {
            assertThat(BoxFsPaths.getFileName("test.txt")).isEqualTo("test.txt");
        }

        @Test
        void shouldGetFileNameOfSimpleFileWithEndingSlash() {
            assertThat(BoxFsPaths.getFileName("C:\\tmp\\test\\")).isEqualTo("");
        }

        @Test
        void shouldReturnNullForNull() {
            assertThat(BoxFsPaths.getFileName(null)).isNull();
        }
    }

    @Nested
    class GetRoot {
        @Test
        void shouldGetRootOfRelativePath() {
            assertThat(BoxFsPaths.getRoot("test")).isNull();
        }

        @Test
        void shouldGetRootOfEmptyPath() {
            assertThat(BoxFsPaths.getRoot("")).isNull();
        }

        @Test
        void shouldGetRootOfNull() {
            assertThat(BoxFsPaths.getRoot(null)).isEqualTo(null);
        }

        @Test
        void shouldGetRootForAbsolutePath() {
            assertThat(BoxFsPaths.getRoot("C:\\a\\b\\c")).isEqualTo("C:\\");
        }

        @Test
        void shouldGetRootForAbsolutePathWithD() {
            assertThat(BoxFsPaths.getRoot("D:\\a\\b\\c")).isEqualTo("D:\\");
        }

        @Test
        void shouldGetRootForDriveLetter() {
            assertThat(BoxFsPaths.getRoot("D:")).isEqualTo("D:\\");
        }
    }

    @Nested
    class GetNameCount {
        @Test
        void shouldGetNameCountOfDirectoryAndFile() {
            assertThat(BoxFsPaths.getNameCount("C:\\directory\\file.txt")).isEqualTo(2);
        }

        @Test
        void shouldGetNameCountOfNestedDirectories() {
            assertThat(BoxFsPaths.getNameCount("C:\\a\\b\\c\\d")).isEqualTo(4);
        }

        @Test
        void shouldGetNameCountOfRootOnly() {
            assertThat(BoxFsPaths.getNameCount("C:\\")).isEqualTo(0);
        }

        @Test
        void shouldGetNameCountOfRelativePath() {
            assertThat(BoxFsPaths.getNameCount("directory\\file.txt")).isEqualTo(2);
        }

        @Test
        void shouldGetNameCountOfEmptyPath() {
            assertThat(BoxFsPaths.getNameCount("")).isEqualTo(0);
        }

        @Test
        void shouldGetNameCountOfNull() {
            assertThat(BoxFsPaths.getNameCount(null)).isEqualTo(0);
        }
    }

    @Nested
    class GetName {
        @Test
        void shouldReturnNullForNull() {
            assertThat(BoxFsPaths.getName(null, 0)).isNull();
            assertThat(BoxFsPaths.getName(null, 1)).isNull();
            assertThat(BoxFsPaths.getName(null, 2)).isNull();
        }

        @Test
        void shouldReturnNameForName() {
            assertThat(BoxFsPaths.getName("directory", 0)).isEqualTo("directory");
            assertThat(BoxFsPaths.getName("directory", 1)).isNull();
        }

        @Test
        void shouldGetNamesOfSimplePath() {
            assertThat(BoxFsPaths.getName("C:\\directory\\file.txt", 0)).isEqualTo("directory");
            assertThat(BoxFsPaths.getName("C:\\directory\\file.txt", 1)).isEqualTo("file.txt");
            assertThat(BoxFsPaths.getName("C:\\directory\\file.txt", 2)).isNull();
        }

        @Test
        void shouldGetNamesOfNestedDirectories() {
            assertThat(BoxFsPaths.getName("C:\\a\\b\\c\\d\\", 0)).isEqualTo("a");
            assertThat(BoxFsPaths.getName("C:\\a\\b\\c\\d\\", 1)).isEqualTo("b");
            assertThat(BoxFsPaths.getName("C:\\a\\b\\c\\d\\", 2)).isEqualTo("c");
            assertThat(BoxFsPaths.getName("C:\\a\\b\\c\\d\\", 3)).isEqualTo("d");
            assertThat(BoxFsPaths.getName("C:\\a\\b\\c\\d\\", 4)).isNull();
        }
    }

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
                assertThat(BoxFsPaths.subpath(path, 0, 2)).isEqualTo("tmp\\test");
            }

            @Test
            void shouldReturnFor0To1() {
                assertThat(BoxFsPaths.subpath(path, 0, 1)).isEqualTo("tmp");
            }

            @Test
            void shouldReturnFor1To2() {
                assertThat(BoxFsPaths.subpath(path, 1, 2)).isEqualTo("test");
            }

            @Test
            void shouldReturnFor0To0() {
                assertThat(BoxFsPaths.subpath(path, 0, 0)).isEqualTo("");
            }

            @Test
            void shouldThrowFor1To3() {
                assertThatThrownBy(() -> BoxFsPaths.subpath(path, 1, 3))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            void shouldThrowForMinus1To1() {
                assertThatThrownBy(() -> BoxFsPaths.subpath(path, -1, 1))
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
                assertThat(BoxFsPaths.subpath(path, 0, 2)).isEqualTo("tmp\\test");
            }

            @Test
            void shouldReturnFor0To1() {
                assertThat(BoxFsPaths.subpath(path, 0, 1)).isEqualTo("tmp");
            }

            @Test
            void shouldReturnFor1To2() {
                assertThat(BoxFsPaths.subpath(path, 1, 2)).isEqualTo("test");
            }

            @Test
            void shouldReturnFor0To0() {
                assertThat(BoxFsPaths.subpath(path, 0, 0)).isEqualTo("");
            }

            @Test
            void shouldThrowFor1To3() {
                assertThatThrownBy(() -> BoxFsPaths.subpath(path, 1, 3))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            void shouldThrowForMinus1To1() {
                assertThatThrownBy(() -> BoxFsPaths.subpath(path, -1, 1))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Test
        void shouldGetSubpathOfSimpleRelativePathStartingWithSeparator() {
            String path = "\\tmp\\test";

            assertThat(BoxFsPaths.subpath(path, 0, 2)).isEqualTo("tmp\\test");
            assertThat(BoxFsPaths.subpath(path, 0, 1)).isEqualTo("tmp");
            assertThat(BoxFsPaths.subpath(path, 1, 2)).isEqualTo("test");
            assertThat(BoxFsPaths.subpath(path, 0, 0)).isEqualTo("");
            assertThatThrownBy(() -> BoxFsPaths.subpath(path, 1, 3))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> BoxFsPaths.subpath(path, -1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldGetSubpathOfSimpleRelativePathEndingWithSeparator() {
            String path = "tmp\\test\\";

            assertThat(BoxFsPaths.subpath(path, 0, 2)).isEqualTo("tmp\\test");
            assertThat(BoxFsPaths.subpath(path, 0, 1)).isEqualTo("tmp");
            assertThat(BoxFsPaths.subpath(path, 1, 2)).isEqualTo("test");
            assertThat(BoxFsPaths.subpath(path, 0, 0)).isEqualTo("");
            assertThatThrownBy(() -> BoxFsPaths.subpath(path, 1, 3))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> BoxFsPaths.subpath(path, -1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldReturnNullForNull() {
            assertThat(BoxFsPaths.subpath(null, 0, 0)).isNull();
            assertThatThrownBy(() -> BoxFsPaths.subpath(null, 0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldReturnForEmptyPath() {
            assertThat(BoxFsPaths.subpath("", 0, 0)).isEqualTo("");
            assertThatThrownBy(() -> BoxFsPaths.subpath("", 0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Resolve {
        @Test
        void shouldResolveRelativePathAgainstAbsolutePath() {
            String result = BoxFsPaths.resolve("C:\\a\\b", "c\\d");
            assertThat(result).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathAgainstAbsolutePath() {
            String result = BoxFsPaths.resolve("C:\\a\\b", "D:\\c\\d");
            assertThat(result).isEqualTo("D:\\c\\d");
        }

        @Test
        void shouldResolveRelativePathAgainstRelativePath() {
            String result = BoxFsPaths.resolve("a\\b", "c\\d");
            assertThat(result).isEqualTo("a\\b\\c\\d");
        }

        @Test
        void shouldResolveRelativePathWithEndingSeparatorAgainstRelativePath() {
            String result = BoxFsPaths.resolve("a\\b\\", "c\\d");
            assertThat(result).isEqualTo("a\\b\\c\\d");
        }

        @Test
        void shouldResolveRelativePathAgainstRelativePathWithStartingSeparator() {
            String result = BoxFsPaths.resolve("a\\b", "\\c\\d");
            assertThat(result).isEqualTo("a\\b\\c\\d");
        }

        @Test
        void shouldResolveRelativePathWithEndlingSeparatorAgainstRelativePathWithStartingSeparator() {
            String result = BoxFsPaths.resolve("a\\b\\", "\\c\\d");
            assertThat(result).isEqualTo("a\\b\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathAgainstRelativePathWithStartingSeparator() {
            String result = BoxFsPaths.resolve("C:\\a\\b", "\\c\\d");
            assertThat(result).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathWithEndingSeparatorAgainstRelativePathWithStartingSeparator() {
            String result = BoxFsPaths.resolve("C:\\a\\b\\", "\\c\\d");
            assertThat(result).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathAgainstRelativePath() {
            String result = BoxFsPaths.resolve("C:\\a\\b", "c\\d");
            assertThat(result).isEqualTo("C:\\a\\b\\c\\d");
        }

        @Test
        void shouldResolveAbsolutePathWithEndingSeparatorAgainstRelativePath() {
            String result = BoxFsPaths.resolve("C:\\a\\b\\", "c\\d");
            assertThat(result).isEqualTo("C:\\a\\b\\c\\d");
        }
    }
}
