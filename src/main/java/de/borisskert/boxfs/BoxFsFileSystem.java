package de.borisskert.boxfs;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BoxFsFileSystem extends FileSystem {
    private static final String SEPARATOR = "/";

    private final AtomicBoolean isOpen = new AtomicBoolean(true);
    private final BoxFsFileSystemProvider provider = new BoxFsFileSystemProvider(SEPARATOR);

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        isOpen.set(false);
    }

    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getSeparator() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Path getPath(String first, String... more) {
        return new BoxFsPath(this, SEPARATOR, first);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
