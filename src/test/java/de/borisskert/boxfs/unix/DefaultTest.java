package de.borisskert.boxfs.unix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@EnabledOnOs(OS.LINUX)
@DisplayName("Linux Paths")
class DefaultTest extends BoxFsPathsTest {
    @Override
    String getParent(String path) {
        Path parent = Paths.get(path).getParent();

        return Optional.ofNullable(parent)
                .map(Object::toString)
                .orElse(null);
    }

    @Override
    boolean isAbsolute(String path) {
        return Paths.get(path).isAbsolute();
    }

    @Override
    String relativize(String path, String other) {
        return Paths.get(path).relativize(Paths.get(other)).toString();
    }

    @Override
    String toAbsolutePath(String path) {
        return Paths.get(path).toAbsolutePath().toString();
    }

    @Override
    String currentWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    @Override
    String getFileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    @Override
    String getRoot(String path) {
        Path root = Paths.get(path).getRoot();

        return Optional.ofNullable(root)
                .map(Object::toString)
                .orElse(null);
    }

    @Override
    int getNameCount(String path) {
        return Paths.get(path).getNameCount();
    }

    @Override
    String getName(String path, int index) {
        return Paths.get(path).getName(index).toString();
    }

    @Override
    String subpath(String path, int beginIndex, int endIndex) {
        return Paths.get(path).subpath(beginIndex, endIndex).toString();
    }

    @Override
    String resolve(String path, String other) {
        return Paths.get(path).resolve(other).toString();
    }

    @Override
    java.util.Iterator<String> iterator(String path) {
        java.util.Iterator<Path> iterator = Paths.get(path).iterator();

        return new java.util.Iterator<String>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public String next() {
                return iterator.next().toString();
            }
        };
    }

    @Override
    String normalize(String path) {
        return Paths.get(path).normalize().toString();
    }
}
