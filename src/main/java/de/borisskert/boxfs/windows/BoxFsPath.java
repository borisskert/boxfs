package de.borisskert.boxfs.windows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

class BoxFsPath implements Path {
    private final BoxFsFileSystem fileSystem;
    private final String path;

    BoxFsPath(BoxFsFileSystem fileSystem, String path) {
        this.fileSystem = fileSystem;
        this.path = path;
    }

    @Override
    public BoxFsFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return path.matches("^[A-Za-z]:\\\\.*");
    }

    @Override
    public BoxFsPath getRoot() {
        String root = BoxFsPaths.getRoot(path);

        return Optional.ofNullable(root)
                .map(path -> new BoxFsPath(fileSystem, path))
                .orElse(null);
    }

    @Override
    public BoxFsPath getFileName() {
        String fileName = BoxFsPaths.getFileName(path);
        return new BoxFsPath(fileSystem, fileName);
    }

    @Override
    public BoxFsPath getParent() {
        return Optional.ofNullable(BoxFsPaths.parentOf(path))
                .map(p -> new BoxFsPath(fileSystem, p))
                .orElse(null);
    }

    @Override
    public int getNameCount() {
        return BoxFsPaths.getNameCount(path);
    }

    @Override
    public BoxFsPath getName(int index) {
        String name = BoxFsPaths.getName(path, index);
        return new BoxFsPath(fileSystem, name);
    }

    @Override
    public BoxFsPath subpath(int beginIndex, int endIndex) {
        String subpath = BoxFsPaths.subpath(this.path, beginIndex, endIndex);
        return new BoxFsPath(fileSystem, subpath);
    }

    @Override
    public boolean startsWith(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean startsWith(String other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean endsWith(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean endsWith(String other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath normalize() {
        return new BoxFsPath(fileSystem, Paths.get(path).normalize().toString());
    }

    @Override
    public BoxFsPath resolve(Path other) {
        String resolved = BoxFsPaths.resolve(path, other.toString());

        return new BoxFsPath(
                fileSystem,
                resolved
        );
    }

    @Override
    public BoxFsPath resolve(String other) {
        return new BoxFsPath(fileSystem, BoxFsPaths.resolve(path, other));
    }

    @Override
    public BoxFsPath resolveSibling(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath resolveSibling(String other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath relativize(Path other) {
        String relativize = BoxFsPaths.relativize(path, other.toString());
        return new BoxFsPath(fileSystem, relativize);
    }

    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }

        String absolutePath = BoxFsPaths.toAbsolutePath(path);
        return new BoxFsPath(fileSystem, absolutePath);
    }

    @Override
    public BoxFsPath toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Iterator<Path> iterator() {
        final Iterator<String> it = BoxFsPaths.iterator(this.path);

        return new Iterator<Path>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Path next() {
                return new BoxFsPath(fileSystem, it.next());
            }
        };
    }

    @Override
    public int compareTo(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        return this.path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BoxFsPath paths = (BoxFsPath) o;
        return Objects.equals(toLowerCase(path), toLowerCase(paths.path));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(toLowerCase(path));
    }

    private static String toLowerCase(String name) {
        if (name == null) {
            return null;
        }

        return name.toLowerCase();
    }
}
