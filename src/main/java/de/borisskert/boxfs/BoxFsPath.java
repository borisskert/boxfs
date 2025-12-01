package de.borisskert.boxfs;

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;

public class BoxFsPath implements Path {
    private final BoxFsFileSystem fileSystem;
    private final String separator;
    private final String path;

    BoxFsPath(BoxFsFileSystem fileSystem, String separator, String path) {
        this.fileSystem = fileSystem;
        this.separator = separator;
        this.path = path;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return path.startsWith(separator);
    }

    @Override
    public Path getRoot() {
        return Paths.get(separator);
    }

    @Override
    public Path getFileName() {
        return Paths.get(path).getFileName();
    }

    @Override
    public Path getParent() {
        return Paths.get(path).getParent();
    }

    @Override
    public int getNameCount() {
        return Paths.get(path).getNameCount();
    }

    @Override
    public Path getName(int index) {
        return Paths.get(path).getName(index);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return Paths.get(path).subpath(beginIndex, endIndex);
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
    public Path normalize() {
        return Paths.get(path).normalize();
    }

    @Override
    public Path resolve(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Path resolve(String other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Path resolveSibling(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Path resolveSibling(String other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Path relativize(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Path toAbsolutePath() {
        return Paths.get(path).toAbsolutePath();
    }

    @Override
    public Path toRealPath(@NotNull LinkOption... options) throws IOException {
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int compareTo(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        return this.path;
    }
}
