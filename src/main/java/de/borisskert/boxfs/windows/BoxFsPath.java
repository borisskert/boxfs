package de.borisskert.boxfs.windows;

import com.sun.istack.internal.NotNull;

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
        Path root = Paths.get(path).getRoot();

        return Optional.ofNullable(root)
                .map(r -> new BoxFsPath(fileSystem, r.toString()))
                .orElse(null);
    }

    @Override
    public BoxFsPath getFileName() {
        return new BoxFsPath(fileSystem, Paths.get(path).getFileName().toString());
    }

    @Override
    public BoxFsPath getParent() {
        return Optional.ofNullable(Paths.get(path).getParent())
                .map(p -> new BoxFsPath(fileSystem, p.toString()))
                .orElse(null);
    }

    @Override
    public int getNameCount() {
        return Paths.get(path).getNameCount();
    }

    @Override
    public BoxFsPath getName(int index) {
        return new BoxFsPath(fileSystem, Paths.get(path).getName(index).toString());
    }

    @Override
    public BoxFsPath subpath(int beginIndex, int endIndex) {
        Path path = Paths.get(this.path);
        Path subpath = path.subpath(beginIndex, endIndex);

        return new BoxFsPath(fileSystem, subpath.toString());
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
        Path resolved = Paths.get(path)
                .resolve(Paths.get(other.toString()));

        return new BoxFsPath(
                fileSystem,
                resolved.toString()
        );
    }

    @Override
    public BoxFsPath resolve(String other) {
        return new BoxFsPath(fileSystem, Paths.get(path).resolve(other).toString());
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
        Path relativized = Paths.get(path).relativize(Paths.get(other.toString()));
        return new BoxFsPath(fileSystem, relativized.toString());
    }

    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath toAbsolutePath() {
        Path absolutePath = Paths.get(path).toAbsolutePath();
        return new BoxFsPath(fileSystem, absolutePath.toString());
    }

    @Override
    public BoxFsPath toRealPath(@NotNull LinkOption... options) throws IOException {
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
        final Iterator<Path> it = Paths.get(path).iterator();

        return new Iterator<Path>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Path next() {
                return new BoxFsPath(fileSystem, it.next().toString());
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
        return Objects.equals(path, paths.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
