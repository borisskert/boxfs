package de.borisskert.boxfs.macos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class BoxFsPathsTest {

    abstract String parentOf(String path);

    @Nested
    class Parent {
        @Test
        void shouldGetParentOfSuperSimplePath() {
            String parent = parentOf("/directory");
            assertThat(parent).isEqualTo("/");
        }

        @Test
        void shouldGetParentOfSimplePath() {
            String parent = parentOf("/directory/file.txt");
            assertThat(parent).isEqualTo("/directory");
        }

        @Test
        void shouldGetParentOfSimpleNestedPath() {
            String parent = parentOf("/a/b/c/d");
            assertThat(parent).isEqualTo("/a/b/c");
        }

        @Test
        void shouldReturnNullForRelativeDirectory() {
            assertThat(parentOf("directory")).isNull();
        }

        @Test
        void shouldGetParentOfRoot() {
            String parent = parentOf("/");
            assertThat(parent).isNull();
        }
    }

    abstract boolean isAbsolute(String path);

    @Nested
    class IsAbsolute {
        @Test
        void shouldReturnTrueForRoot() {
            assertThat(isAbsolute("/")).isTrue();
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
            assertThat(isAbsolute("/")).isTrue();
        }

        @Test
        void shouldThrowForRootWithSeparator() {
            assertThat(isAbsolute("//")).isTrue();
        }

        @Test
        void shouldReturnFalseForSimpleFile() {
            assertThat(isAbsolute("file.txt")).isFalse();
        }

        @Test
        void shouldReturnFalseForSimpleFileStartingWithSlash() {
            assertThat(isAbsolute("/file.txt")).isTrue();
        }

        @Test
        void shouldReturnFalseForSimpleFileStartingWithDot() {
            assertThat(isAbsolute("./file.txt")).isFalse();
        }
    }

    abstract String relativize(String path, String other);

    @Nested
    class Relativize {

        @Test
        void shouldRelativizeTheSameDirectory() {
            String relativized = relativize("/directory", "/directory");
            assertThat(relativized).isEqualTo("");
        }

        @Test
        void shouldRelativizeTheSameDirectoryWithEndingSlash() {
            String relativized = relativize("/directory", "/directory/");
            assertThat(relativized).isEqualTo("");
        }

        @Test
        void shouldRelativizeTheSameDirectoryWithEndingSlashInOppositeDirection() {
            String relativized = relativize("/directory/", "/directory");
            assertThat(relativized).isEqualTo("");
        }

        @Test
        void shouldRelativizeFileInSameDirectory() {
            String relativized = relativize("/directory", "/directory/file.txt");
            assertThat(relativized).isEqualTo("file.txt");
        }

        @Test
        void shouldRelativizeFileInSameDirectoryWithEndingSeparator() {
            String relativized = relativize("/directory/", "/directory/file.txt");
            assertThat(relativized).isEqualTo("file.txt");
        }

        @Test
        void shouldRelativizeFileInSameDirectoryInOtherDirection() {
            String relativized = relativize("/directory/file.txt", "/directory");
            assertThat(relativized).isEqualTo("..");
        }

        @Test
        void shouldRelativizeTwoLevels() {
            String relativized = relativize("/a/b/c/d", "/a");
            assertThat(relativized).isEqualTo("../../..");
        }

        @Test
        void shouldRelativizeTwoLevelsInOppositeDirection() {
            String relativized = relativize("/a", "/a/b/c/d");
            assertThat(relativized).isEqualTo("b/c/d");
        }

        @Test
        void shouldRelativizeNestedPath() {
            String relativized = relativize("/tmp/a/b/c/d", "/tmp/a/b/c/d/test");
            assertThat(relativized).isEqualTo("test");
        }

        @Test
        void shouldRelativizeSimplePath() {
            String relativized = relativize("/", "/tmp/testdir");
            assertThat(relativized).isEqualTo("tmp/testdir");
        }
    }

    abstract String toAbsolutePath(String path);

    abstract String currentWorkingDirectory();

    @Nested
    class ToAbsolutePath {
        @Test
        void shouldReturnAbsolutePathOfSimplestRelativePath() {
            assertThat(toAbsolutePath("test")).isEqualTo(currentWorkingDirectory() + "/test");
        }

        @Test
        void shouldReturnAbsolutePathOfSimpleRelativePath() {
            assertThat(toAbsolutePath("tmp/test")).isEqualTo(currentWorkingDirectory() + "/tmp/test");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePath() {
            assertThat(toAbsolutePath("a/b/c/d")).isEqualTo(currentWorkingDirectory() + "/a/b/c/d");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithEndingSlash() {
            assertThat(toAbsolutePath("a/b/c/d/")).isEqualTo(currentWorkingDirectory() + "/a/b/c/d");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithStartingSlash() {
            assertThat(toAbsolutePath("/a/b/c/d/")).isEqualTo("/a/b/c/d");
        }

        @Test
        void shouldReturnAbsolutePathOfRelativePathWithDoubleStartingSlash() {
            assertThat(toAbsolutePath("//a/b/c/d/")).isEqualTo("/a/b/c/d");
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
            assertThat(toAbsolutePath("/a/b/c/d")).isEqualTo("/a/b/c/d");
        }
    }

    abstract String getFileName(String path);

    @Nested
    class GetFileName {
        @Test
        void shouldGetFileNameOfSimpleFile() {
            assertThat(getFileName("/tmp/test")).isEqualTo("test");
        }

        @Test
        void shouldGetFileNameOfSimpleFileWithExtension() {
            assertThat(getFileName("test.txt")).isEqualTo("test.txt");
        }

        @Test
        void shouldGetFileNameOfSimpleFileWithEndingSlash() {
            assertThat(getFileName("/tmp/test/")).isEqualTo("test");
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
            assertThat(getRoot("/a/b/c")).isEqualTo("/");
        }

        @Test
        void shouldGetRootForDrivePathC() {
            assertThat(getRoot("/")).isEqualTo("/");
        }
    }

    abstract int getNameCount(String path);

    @Nested
    class GetNameCount {
        @Test
        void shouldGetNameCountOfDirectoryAndFile() {
            assertThat(getNameCount("/directory/file.txt")).isEqualTo(2);
        }

        @Test
        void shouldGetNameCountOfNestedDirectories() {
            assertThat(getNameCount("/a/b/c/d")).isEqualTo(4);
        }

        @Test
        void shouldGetNameCountOfRootOnly() {
            assertThat(getNameCount("/")).isEqualTo(0);
        }

        @Test
        void shouldGetNameCountOfRelativePath() {
            assertThat(getNameCount("directory/file.txt")).isEqualTo(2);
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
            assertThat(getName("/directory/file.txt", 0)).isEqualTo("directory");
            assertThat(getName("/directory/file.txt", 1)).isEqualTo("file.txt");
            assertThatThrownBy(() -> getName("/directory/file.txt", 2)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldGetNamesOfNestedDirectories() {
            assertThat(getName("/a/b/c/d/", 0)).isEqualTo("a");
            assertThat(getName("/a/b/c/d/", 1)).isEqualTo("b");
            assertThat(getName("/a/b/c/d/", 2)).isEqualTo("c");
            assertThat(getName("/a/b/c/d/", 3)).isEqualTo("d");
            assertThatThrownBy(() -> getName("/a/b/c/d/", 4)).isInstanceOf(IllegalArgumentException.class);
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
                path = "/tmp/test";
            }

            @Test
            void shouldReturnFor0To2() {
                assertThat(subpath(path, 0, 2)).isEqualTo("tmp/test");
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
                path = "tmp/test";
            }

            @Test
            void shouldReturnFor0To2() {
                assertThat(subpath(path, 0, 2)).isEqualTo("tmp/test");
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
            String path = "/tmp/test";

            assertThat(subpath(path, 0, 2)).isEqualTo("tmp/test");
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
            String path = "tmp/test/";

            assertThat(subpath(path, 0, 2)).isEqualTo("tmp/test");
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
            String result = resolve("/a/b", "c/d");
            assertThat(result).isEqualTo("/a/b/c/d");
        }

        @Test
        void shouldResolveRelativePathAgainstRelativePath() {
            String result = resolve("a/b", "c/d");
            assertThat(result).isEqualTo("a/b/c/d");
        }

        @Test
        void shouldResolveRelativePathWithEndingSeparatorAgainstRelativePath() {
            String result = resolve("a/b/", "c/d");
            assertThat(result).isEqualTo("a/b/c/d");
        }

        @Test
        void shouldResolveRelativePathAgainstRelativePathWithStartingSeparator() {
            String result = resolve("a/b", "/c/d");
            assertThat(result).isEqualTo("/c/d");
        }

        @Test
        void shouldResolveRelativePathWithEndlingSeparatorAgainstRelativePathWithStartingSeparator() {
            String result = resolve("a/b/", "/c/d");
            assertThat(result).isEqualTo("/c/d");
        }

        @Test
        void shouldResolveAbsolutePathAgainstRelativePathWithStartingSeparator() {
            String result = resolve("/a/b", "/c/d");
            assertThat(result).isEqualTo("/c/d");
        }

        @Test
        void shouldResolveAbsolutePathWithEndingSeparatorAgainstRelativePathWithStartingSeparator() {
            String result = resolve("/a/b/", "/c/d");
            assertThat(result).isEqualTo("/c/d");
        }

        @Test
        void shouldResolveAbsolutePathAgainstRelativePath() {
            String result = resolve("/a/b", "c/d");
            assertThat(result).isEqualTo("/a/b/c/d");
        }

        @Test
        void shouldResolveAbsolutePathWithEndingSeparatorAgainstRelativePath() {
            String result = resolve("/a/b/", "c/d");
            assertThat(result).isEqualTo("/a/b/c/d");
        }

        @Test
        void shouldResolveTestDir() {
            String result = resolve("/", "tmp/testdir");
            assertThat(result).isEqualTo("/tmp/testdir");
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
            java.util.Iterator<String> iterator = iterator("/");
            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleAbsoluteNestedPath() {
            String path = "/tmp/testDir";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleAbsoluteNestedPathWithEndingSeparator() {
            String path = "/tmp/testDir/";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldThrowWhenIterateOverSimpleAbsoluteNestedPathWithStartingSeparator() {
            String path = "//tmp/testDir";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldThrowForSimpleAbsoluteNestedPathWithStartingAndEndingSeparator() {
            String path = "//tmp/testDir/";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleRelativeNestedPath() {
            String path = "tmp/testDir";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleRelativeNestedPathWithStartingSeparator() {
            String path = "/tmp/testDir";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleRelativeNestedPathWithEndingSeparator() {
            String path = "tmp/testDir/";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }

        @Test
        void shouldIterateOverSimpleRelativeNestedPathWithStartingAndEndingSeparator() {
            String path = "/tmp/testDir/";
            java.util.Iterator<String> iterator = iterator(path);

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("tmp");

            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("testDir");

            assertThat(iterator.hasNext()).isFalse();
        }
    }
}
