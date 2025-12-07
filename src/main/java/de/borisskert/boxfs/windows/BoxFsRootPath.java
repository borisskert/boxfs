package de.borisskert.boxfs.windows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;

class BoxFsRootPath extends BoxFsPath {

    BoxFsRootPath(BoxFsFileSystem fileSystem) {
        super(fileSystem, fileSystem.separator());
    }

    @Override
    public BoxFsFileSystem getFileSystem() {
        return super.getFileSystem();
    }

    @Override
    public boolean isAbsolute() {
        return super.isAbsolute();
    }

    @Override
    public BoxFsPath getRoot() {
        return this;
    }

    @Override
    public BoxFsPath getFileName() {
        return super.getFileName();
    }

    @Override
    public BoxFsPath getParent() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int getNameCount() {
        return 0;
    }

    @Override
    public BoxFsPath getName(int index) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath subpath(int beginIndex, int endIndex) {
        throw new UnsupportedOperationException("Not yet implemented");
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath resolve(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath resolve(String other) {
        throw new UnsupportedOperationException("Not yet implemented");
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
        throw new UnsupportedOperationException("Not yet implemented");
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

        throw new UnsupportedOperationException("Not yet implemented");
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int compareTo(Path other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
