package de.borisskert.boxfs.windows;

import org.junit.jupiter.api.DisplayName;

@DisplayName("BoxFsPaths")
class BoxFsTest extends BoxFsPathsTest {
    @Override
    String getParent(String path) {
        return BoxFsPaths.parentOf(path);
    }

    @Override
    boolean isAbsolute(String path) {
        return BoxFsPaths.isAbsolute(path);
    }

    @Override
    String relativize(String path, String other) {
        return BoxFsPaths.relativize(path, other);
    }

    @Override
    String toAbsolutePath(String path) {
        return BoxFsPaths.toAbsolutePath(path);
    }

    @Override
    String currentWorkingDirectory() {
        return "C:";
    }

    @Override
    String currentRoot() {
        return "C:\\";
    }

    @Override
    String getFileName(String path) {
        return BoxFsPaths.getFileName(path);
    }

    @Override
    String getRoot(String path) {
        return BoxFsPaths.getRoot(path);
    }

    @Override
    int getNameCount(String path) {
        return BoxFsPaths.getNameCount(path);
    }

    @Override
    String getName(String path, int index) {
        return BoxFsPaths.getName(path, index);
    }

    @Override
    String subpath(String path, int beginIndex, int endIndex) {
        return BoxFsPaths.subpath(path, beginIndex, endIndex);
    }

    @Override
    String resolve(String path, String other) {
        return BoxFsPaths.resolve(path, other);
    }

    @Override
    java.util.Iterator<String> iterator(String path) {
        return BoxFsPaths.iterator(path);
    }

    @Override
    String normalize(String path) {
        return BoxFsPaths.normalize(path);
    }
}
